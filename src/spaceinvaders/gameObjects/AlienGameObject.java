package spaceinvaders.gameObjects;

import spaceinvaders.Game;

/**
 * An gameObject which represents one of our space invader aliens.
 *
 * @author Original code base - Kevin Glass, refactors - Andrew Lem
 */
public class AlienGameObject extends GameObject {
    public static final int DEFAULT_ALIEN_MOVE_SPEED = 75;
    public static final double DEFAULT_ALIEN_MOVE_SPEED_INCREASE = 1.02;
    /**
     * The speed at which the alien moves horizontally
     */
    private double moveSpeed = DEFAULT_ALIEN_MOVE_SPEED;
    /**
     * The game in which the gameObject exists
     */
    private Game game;

    /**
     * Create a new alien gameObject
     *
     * @param game The game in which this gameObject is being created
     * @param ref  The sprite which should be displayed for this alien
     * @param x    The initial x location of this alien
     * @param y    The initial y location of this alien
     */
    public AlienGameObject(Game game, String ref, int x, int y) {
        super(ref, x, y);

        this.game = game;
        dx = -moveSpeed;
    }

    /**
     * Request that this alien moved based on time elapsed
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
        if ((dx > 0) && (x > Game.MAX_X - Game.SCREEN_EDGE_BUFFER)) {
            game.updateLogic();
        }

        // proceed with normal move
        super.move(delta);
    }

    /**
     * Update the game logic related to aliens
     */
    public void doLogic() {
        // swap over horizontal movement and move down the
        // screen a bit
        dx = -dx;
        y += 10;

        // if we've reached the bottom of the screen then the player
        // dies
        if (y > Game.MAX_Y - Game.SCREEN_EDGE_BUFFER) {
            game.notifyDeath();
        }
    }

    /**
     * Notification that this alien has collided with another gameObject
     *
     * @param other The other gameObject
     */
    public void collidedWith(GameObject other) {
        // collisions with aliens are handled elsewhere
    }

    public void increaseMovementSpeed() {
        setHorizontalMovement(getHorizontalMovement() * DEFAULT_ALIEN_MOVE_SPEED_INCREASE);
    }
}