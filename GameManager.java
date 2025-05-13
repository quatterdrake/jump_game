import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static GameManager instance;

    private int score;
    private int highscore;
    private boolean alive;
    private boolean started;

    private List<GameEventListener> listeners = new ArrayList<>();

    private GameManager() {
        score = 0;
        highscore = 0;
        alive = true;
        started = false;
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public int getHighscore() {
        return highscore;
    }
    public void setHighscore(int highscore) {
        if (highscore > this.highscore) {
            this.highscore = highscore;
            notifyNewHighscore(highscore);
        } else {
            this.highscore = highscore;
        }
    }
    public boolean isAlive() {
        return alive;
    }
    public void setAlive(boolean alive) {
        if (this.alive && !alive) {
            notifyPlayerDeath();
        }
        this.alive = alive;
    }
    public boolean isStarted() {
        return started;
    }
    public void setStarted(boolean started) {
        this.started = started;
    }

    public void reset() {
        score = 0;
        alive = true;
        started = false;
    }

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }
    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }
    private void notifyPlayerDeath() {
        for (GameEventListener l : listeners) l.onPlayerDeath();
    }
    private void notifyNewHighscore(int score) {
        for (GameEventListener l : listeners) l.onNewHighscore(score);
    }
} 