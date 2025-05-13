public class GroundedState implements PlayerState {
    @Override
    public void update(Astrojump game) {
        // Фрикция
        if (!game.leftPressed && !game.rightPressed) {
            if (game.xVel > 0)
                game.xVel -= Astrojump.FRICTION / Astrojump.FPS;
            else if (game.xVel < 0)
                game.xVel += Astrojump.FRICTION / Astrojump.FPS;
        }
        // Прыжок
        if (game.upPressed) {
            game.setState(new JumpingState());
            game.yVel = Astrojump.JUMP_SPEED + Math.abs(game.xVel) / 2;
        }
    }
    @Override
    public void handleInput(Astrojump game) {
        // Обработка ввода для движения
        if (game.leftPressed && Math.abs(game.xVel) <= Astrojump.MAX_HORIZ_SPEED)
            game.xVel -= Astrojump.HORIZ_ACCEL / Astrojump.FPS;
        if (game.rightPressed && Math.abs(game.xVel) <= Astrojump.MAX_HORIZ_SPEED)
            game.xVel += Astrojump.HORIZ_ACCEL / Astrojump.FPS;
    }
} 