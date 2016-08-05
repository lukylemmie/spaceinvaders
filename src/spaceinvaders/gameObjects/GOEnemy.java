package spaceinvaders.gameObjects;

import spaceinvaders.Game;

/**
 * An gameObject which represents an enemy.
 *
 * @author Andrew Lem
 */
public class GOEnemy extends GameObject {
    public static final int DEFAULT_ENEMY_MOVE_SPEED = 75;
    public static final double DEFAULT_ENEMY_MOVE_SPEED_INCREASE = 1.02;
    private double moveSpeed = DEFAULT_ENEMY_MOVE_SPEED;

    /**
     * Create a new enemy gameObject
     *
     * @param game The game in which this gameObject is being created
     * @param ref  The sprite which should be displayed for this enemy
     * @param x    The initial x location of this enemy
     * @param y    The initial y location of this enemy
     */
    public GOEnemy(Game game, String ref, int x, int y) {
        super(game, ref, x, y);

        dx = -moveSpeed;
    }

    /**
     * Request that this enemy moved based on time elapsed
     *
     * @param delta The time that has elapsed since last move
     */
    public void move(long delta) {
        // if we have reached the left hand side of the screen and
        // are moving left then request a logic update
        if ((dx < 0) && (x < 10)) {
            game.updateLogic();
        }
        // and vice vesa, if we have reached the right hand side of
        // the screen and are moving right, request a logic update
        if ((dx > 0) && (x > Game.MAX_X - Game.SCREEN_EDGE_INNER_BUFFER)) {
            game.updateLogic();
        }

        // proceed with normal move
        super.move(delta);
    }

    /**
     * Update the game logic related to enemies
     */
    public void doLogic() {
        // swap over horizontal movement and move down the
        // screen a bit
        dx = -dx;
        y += 10;

        // if we've reached the bottom of the screen then the player
        // dies
        if (y > Game.MAX_Y - Game.SCREEN_EDGE_INNER_BUFFER) {
            game.notifyDeath();
        }
    }

    /**
     * Notification that this enemy has collided with another gameObject
     *
     * @param other The other gameObject
     */
    public void collidedWith(GameObject other) {
        // collisions with enemies are handled elsewhere
    }

    public void increaseMovementSpeed() {
        setHorizontalMovement(getHorizontalMovement() * DEFAULT_ENEMY_MOVE_SPEED_INCREASE);
    }
}