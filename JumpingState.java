public class JumpingState implements PlayerState {
    @Override
    public void update(Astrojump game) {
        // Гравитация
        game.yVel += Astrojump.GRAVITY / Astrojump.FPS;
        // Движение
        game.x += game.xVel / Astrojump.FPS;
        game.y += game.yVel / Astrojump.FPS;

        // Проверка смерти
        if (game.frameY - game.y - Astrojump.PLAYER_HEIGHT > Astrojump.FRAME_HEIGHT) {
            GameManager.getInstance().setAlive(false);
            game.setState(new DeadState());
            return;
        }

        // Проверка приземления на платформу
        for (int i = 0; i < game.platforms.length; ++i) {
            if (game.yVel <= 0 &&
                game.x > game.platforms[i].getX() - Astrojump.PLAYER_WIDTH &&
                game.x < game.platforms[i].getX() + game.platforms[i].getLength() &&
                game.y <= game.platforms[i].getY() + Astrojump.PLATFORM_THICKNESS &&
                game.y > game.platforms[i].getY() - Astrojump.FALL_LENIENCE) {
                game.y = game.platforms[i].getY() + Astrojump.PLATFORM_THICKNESS;
                game.yVel = 0;
                game.setState(new GroundedState());
                return;
            }
        }
    }
    @Override
    public void handleInput(Astrojump game) {
        // Движение в воздухе
        if (game.leftPressed && Math.abs(game.xVel) <= Astrojump.MAX_HORIZ_SPEED)
            game.xVel -= Astrojump.HORIZ_ACCEL / Astrojump.FPS;
        if (game.rightPressed && Math.abs(game.xVel) <= Astrojump.MAX_HORIZ_SPEED)
            game.xVel += Astrojump.HORIZ_ACCEL / Astrojump.FPS;
    }
} 