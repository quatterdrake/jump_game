public class DoubleJumpStateDecorator implements PlayerState {
    private PlayerState wrappedState;
    private boolean doubleJumpAvailable = true;
    private static final double DOUBLE_JUMP_MULTIPLIER = 1.7;

    public DoubleJumpStateDecorator(PlayerState wrappedState) {
        this.wrappedState = wrappedState;
    }

    @Override
    public void update(Astrojump game) {
        wrappedState.update(game);
        // Если игрок приземлился, сбрасываем возможность двойного прыжка
        if (game.getState() instanceof GroundedState) {
            doubleJumpAvailable = true;
        }
    }

    @Override
    public void handleInput(Astrojump game) {
        // Если игрок в воздухе и нажал прыжок, и двойной прыжок доступен
        if (game.upPressed && doubleJumpAvailable && game.getState() instanceof JumpingState) {
            game.yVel = (int)(Astrojump.JUMP_SPEED * DOUBLE_JUMP_MULTIPLIER + Math.abs(game.xVel) / 2);
            doubleJumpAvailable = false;
        } else {
            wrappedState.handleInput(game);
        }
    }
} 