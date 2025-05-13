import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.*;


public class Astrojump extends JPanel implements ActionListener, KeyListener, GameEventListener
{
	public Scanner f;
	public Timer timer; 
	public int x;
	// This y value is how high the character is in the level
	public int y;
	public int xVel;
	public int yVel;

	public int parachuteX;
	public int parachuteY;
	public int parachuteXVel;
	public int parachuteYVel;

	public boolean leftPressed;
	public boolean rightPressed;
	public boolean upPressed;
	public boolean downPressed;
	public boolean enterPressed;
	public boolean qPressed;
	public boolean rPressed;

	public Platform[] platforms = new Platform[5];
	public int curPlatformHeight;

	// Where the top left corner pixel is within the level
	public int frameY;
	public int scrollSpeed;
	public int scrollUpCounter;
	
	public BufferedImage bg;
	public BufferedImage startImage;
	public BufferedImage deathImage;
	public BufferedImage character;
	public BufferedImage characterSquat;
	public BufferedImage parachute;

	public static final int HORIZ_ACCEL = 2000;
	public static final int MAX_HORIZ_SPEED = 1250;
	public static final int JUMP_SPEED = 1000;
	// Make it too low, the player falls through the floor
	// Make it too high, the player lands on platforms he shouldn't
	public static final int FALL_LENIENCE = 12;
	public static final int FRAME_WIDTH = 480;
	public static final int FRAME_HEIGHT = 640;
	public static final int PLATFORM_THICKNESS = 8;
	public static final int FIRST_PLATFORM_Y = FRAME_HEIGHT / 6;
	public static final int PLAYER_WIDTH = 30;
	public static final int PLAYER_HEIGHT = 40;
	public static final int PARACHUTE_WIDTH = 72;
	public static final int PARACHUTE_HEIGHT = 96;
	public static final int PLAYER_START_X = FRAME_WIDTH / 2 - PLAYER_WIDTH / 2;
	public static final int PLAYER_START_Y = FIRST_PLATFORM_Y + PLATFORM_THICKNESS;
	public static final int GRAVITY = -1750;
	public static final int FRICTION = 1500;
	public static final int PARACHUTE_SWAY = 400;
	public static final int PLATFORM_DIFFERENCE = FRAME_HEIGHT / 3;
	// The space between the top of the frame and the player
	public static final int FRAME_GAP = FRAME_HEIGHT / 4;
	public static final int FPS = 60;
	public static final int SCROLL_SPEED_UP_TIME = FPS * 20;
	public static final int BG_SCROLL_PER_Y = 10;

	private PlayerState state;

	public Astrojump()
	{
		x = PLAYER_START_X;
		y = PLAYER_START_Y;
		xVel = 400;
		yVel = 0;

		parachuteX = PLAYER_START_X;
		parachuteY = 2 * FRAME_WIDTH / 5;
		parachuteXVel = -250;
		parachuteYVel = 60;

		leftPressed = false;
		rightPressed = false;
		upPressed = false;
		downPressed = false;
		enterPressed = false;
		qPressed = false;
		rPressed = false;

		frameY = FRAME_HEIGHT;
		scrollSpeed = 1;
		scrollUpCounter = 0;

		// Open images
		try { bg = ImageIO.read(new File("res/bg.png")); }
		catch (Exception e) { System.out.println("Missing background image"); }
		try { startImage = ImageIO.read(new File("res/start.png")); }
		catch (Exception e) { System.out.println("Missing start screen image"); }
		try { deathImage = ImageIO.read(new File("res/death.png")); }
		catch (Exception e) { System.out.println("Missing death screen image"); }
		try { character = ImageIO.read(new File("res/character.png")); }
		catch (Exception e) { System.out.println("Missing character image"); }
		try { characterSquat = ImageIO.read(new File("res/squat.png")); }
		catch (Exception e) { System.out.println("Missing character squat image"); }
		try { parachute = ImageIO.read(new File("res/parachute.png")); }
		catch (Exception e) { System.out.println("Missing parachute image"); }

		// Load highscore
		GameManager gm = GameManager.getInstance();
		gm.setScore(0);
		try
		{
			File highscoreFile = new File(".highscore");
			highscoreFile.createNewFile();
			Scanner reader = new Scanner(highscoreFile);
			if (reader.hasNextInt()) {
				gm.setHighscore(reader.nextInt());
			}
		}
		catch (Exception e){}

		// Create the first platform and loop to construct the rest
		platforms[0] = PlatformFactory.createPlatform(0, FIRST_PLATFORM_Y, FRAME_WIDTH);
		curPlatformHeight = FRAME_HEIGHT / 2;
		for (int i = 1; i < platforms.length; ++i)
		{
			platforms[i] = PlatformFactory.createPlatform(curPlatformHeight);
			curPlatformHeight += PLATFORM_DIFFERENCE;
		}

		// Allow JPanel to receive input
		addKeyListener(this);
		setFocusable(true);

		// Start the timer
		timer = new Timer(1000 / FPS, this);
		timer.start();

		// Register this as a listener for game events
		GameManager.getInstance().addListener(this);

		this.state = new GroundedState();
	}

	public void paint(Graphics gOld)
	{
		Graphics2D g = (Graphics2D) gOld;
		
		// Set antialiasing
		g.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

		// Clears screen
		super.paintComponent(g);
		if (GameManager.getInstance().isStarted())
		{
			if (!GameManager.getInstance().isAlive())
			{
				paintDeathScreen(g);
			}
			else
			{
				paintBg(g);
				paintPlatforms(g);
				paintGround(g);
				paintPlayer(g);
				paintScore(g);
			}
		}
		else
		{
			paintStartScreen(g);
			paintPlayer(g);
		}
		
		// For Linux: syncs graphics buffers to keep display up-to-date
		Toolkit.getDefaultToolkit().sync();	
	}

	public void paintPlayer(Graphics2D g)
	{
		if (downPressed)
			g.drawImage(characterSquat, x, frameY - y - PLAYER_HEIGHT, null);
		else
			g.drawImage(character, x, frameY - y - PLAYER_HEIGHT, null);
	}

	public void paintPlatforms(Graphics2D g)
	{
		g.setColor(new Color(0x966548));
		
		for (int i = 0; i < platforms.length; ++i)
		{
			g.fillRect(platforms[i].getX(), frameY - platforms[i].getY() - PLATFORM_THICKNESS,
				platforms[i].getLength(), PLATFORM_THICKNESS);
		}
	}

	public void paintGround(Graphics2D g)
	{
		g.setColor(new Color(0x1CA234));
		g.fillRect(0, frameY - FIRST_PLATFORM_Y - PLATFORM_THICKNESS, FRAME_WIDTH, 300);
	}

	public void paintScore(Graphics2D g)
	{
		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 60));
		g.drawString("Score: " + GameManager.getInstance().getScore(), 0, FRAME_HEIGHT - 4);
	}

	public void paintBg(Graphics2D g)
	{
		if (FRAME_HEIGHT - bg.getHeight(null) + y / BG_SCROLL_PER_Y < 0)
			g.drawImage(bg, 0, FRAME_HEIGHT - bg.getHeight(null) + y / BG_SCROLL_PER_Y, null);
		else
			g.drawImage(bg, 0, 0, null);
	}

	public void paintStartScreen(Graphics2D g)
	{
		g.drawImage(startImage, 0, 0, null);
	}

	public void paintDeathScreen(Graphics2D g)
	{
		g.drawImage(deathImage, 0, 0, null);

		// Draw score and highscore
		g.setColor(Color.WHITE);
		Font f = new Font(Font.SANS_SERIF, Font.ITALIC, 40);
		g.setFont(f);
		FontMetrics metrics = g.getFontMetrics();
		g.drawString("Score: " + GameManager.getInstance().getScore(), FRAME_WIDTH / 2 - metrics.stringWidth("Score: " + GameManager.getInstance().getScore()) / 2, 390);
		g.drawString("Highscore: " + GameManager.getInstance().getHighscore(), FRAME_WIDTH / 2 - metrics.stringWidth("Highscore: " + GameManager.getInstance().getHighscore()) / 2, 440);

		g.drawImage(parachute, parachuteX, parachuteY, null);
	}

	public void update()
	{
		if (GameManager.getInstance().isStarted())
		{
			updatePlatforms();
			updatePlayer();
			if (!GameManager.getInstance().isAlive())
				parachutePlayer();
		}
		else
		{
			bouncePlayer();
		}
	}

	public void updatePlayer()
	{
		state.handleInput(this);
		state.update(this);
		// Scroll screen
		if (y + FRAME_GAP > frameY)
			frameY = y + FRAME_GAP;
		if (scrollUpCounter == SCROLL_SPEED_UP_TIME)
		{
			scrollSpeed += 1;
			scrollUpCounter = 0;
		}
		++scrollUpCounter;
		frameY += scrollSpeed;
		// Set score
		GameManager gm = GameManager.getInstance();
		gm.setScore(Math.max((y - FIRST_PLATFORM_Y) / PLATFORM_DIFFERENCE, gm.getScore()));
	}
	
	// How player acts on start screen
	public void bouncePlayer()
	{
		// Bounce off walls
		if (x < 0 || x + PLAYER_WIDTH > FRAME_WIDTH)
			xVel = -xVel;
		
		// Bounce on floor
		if (y < FIRST_PLATFORM_Y + PLATFORM_THICKNESS)
		{
			y = FIRST_PLATFORM_Y + PLATFORM_THICKNESS;
			yVel = JUMP_SPEED;
		}

		// Fall
		yVel += GRAVITY / FPS;

		// Move
		x += xVel / FPS;
		y += yVel / FPS;
	}
	
	public void parachutePlayer()
	{
		// Sway parachute
		if (parachuteX + PARACHUTE_WIDTH / 2 < FRAME_WIDTH / 2)
			parachuteXVel += PARACHUTE_SWAY / FPS;
		else
			parachuteXVel -= PARACHUTE_SWAY / FPS;

		// Teleport to the top if fallen below screen
		if (parachuteY > FRAME_HEIGHT)
			parachuteY = -PARACHUTE_HEIGHT;

		// Move
		parachuteX += parachuteXVel / FPS;
		parachuteY += parachuteYVel / FPS;
	}

	public void updatePlatforms()
	{
		for (int i = 0; i < platforms.length; ++i)
		{
			if (platforms[i].getY() + PLATFORM_THICKNESS + PLAYER_HEIGHT < frameY - FRAME_HEIGHT)
			{
				platforms[i].reset(curPlatformHeight);
				curPlatformHeight += PLATFORM_DIFFERENCE;
			}
		}
	}
		

	public void actionPerformed(ActionEvent e)
	{
		update();
		repaint();
	}
	
	public void keyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();

		if (key == KeyEvent.VK_LEFT)
			leftPressed = true;
		else if (key == KeyEvent.VK_RIGHT)
			rightPressed = true;
		else if (key == KeyEvent.VK_UP)
			upPressed = true;
		else if (key == KeyEvent.VK_DOWN)
			downPressed = true;
		else if (key == KeyEvent.VK_ENTER)
			enterPressed = true;
		else if (key == KeyEvent.VK_Q)
			qPressed = true;
		else if (key == KeyEvent.VK_R)
			rPressed = true;
		else if (key == KeyEvent.VK_D) {
			// Включаем двойной прыжок
			setState(new DoubleJumpStateDecorator(getState()));
			System.out.println("[POWER-UP] Double jump enabled!");
		}
	}

	public void keyTyped(KeyEvent e){}

	public void keyReleased(KeyEvent e)
	{
		int key = e.getKeyCode();

		if (key == KeyEvent.VK_LEFT)
			leftPressed = false;
		else if (key == KeyEvent.VK_RIGHT)
			rightPressed = false;
		else if (key == KeyEvent.VK_UP)
			upPressed = false;
		else if (key == KeyEvent.VK_DOWN)
			downPressed = false;
		else if (key == KeyEvent.VK_ENTER)
			enterPressed = false;
		else if (key == KeyEvent.VK_Q)
			qPressed = false;
		else if (key == KeyEvent.VK_R)
			rPressed = false;
	}

	public static void main(String[] args)
	{
		boolean firstTime = true;

		while (true)
		{
			JFrame frame = new JFrame("Astrojump");
			Astrojump game = new Astrojump();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.add(game);
			frame.setResizable(false);
			frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
			frame.setLocationRelativeTo(null);
			frame.setUndecorated(true);
			frame.setVisible(true);

			// Only show start screen on the first time running
			if (firstTime)
			{
				firstTime = false;
				while (!game.enterPressed) delay(10);
			}
			// The player bounces in the start screen: reset him
			game.x = PLAYER_START_X;
			game.y = PLAYER_START_Y;
			game.xVel = 0;
			game.yVel = 0;
			GameManager.getInstance().setStarted(true);

			while (GameManager.getInstance().isAlive()) delay(10);

			// Write new highscore
			if (GameManager.getInstance().getScore() > GameManager.getInstance().getHighscore())
			{
				try
				{
					FileWriter highscoreWriter = new FileWriter(".highscore");
					highscoreWriter.write(GameManager.getInstance().getScore() + "\n");
					highscoreWriter.close();
				}
				catch (Exception e)
				{
					System.out.println("File write failed");
				}
			}

			while (!game.qPressed && !game.rPressed) delay(10);
			game.timer.stop();

			// Сброс состояния GameManager перед новым запуском
			GameManager.getInstance().reset();

			frame.dispose();

			if (game.qPressed)
				break;
		}
	}

	public static void delay(int ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch (InterruptedException e)
		{
			System.out.println("Delay failed");
		}
	}

	@Override
	public void onPlayerDeath() {
		System.out.println("[EVENT] Player died!");
	}

	@Override
	public void onNewHighscore(int score) {
		System.out.println("[EVENT] New highscore: " + score);
	}

	public void setState(PlayerState state) {
		this.state = state;
	}

	public PlayerState getState() {
		return state;
	}
}
