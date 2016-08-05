package spaceinvaders.gameObjects;

import spaceinvaders.Game;

import static spaceinvaders.Game.MAX_X;

/**
 * The gameObject that represents the players ship
 *
 * @author Original code base - Kevin Glass, refactors - Andrew Lem
 */
public class ShipGameObject extends GameObject {
    public static final int DEFAULT_SHIP_MOVE_SPEED = 300;
    public static final int DEFAULT_FIRING_INTERVAL = 500;
    /**
     * The speed at which the player's ship should move (pixels/sec)
     */
    private double moveSpeed = DEFAULT_SHIP_MOVE_SPEED;

    /**
     * The time at which last fired a bullet
     */
    private long lastFire = 0;
    /**
     * The interval between our players bullet (ms)
     */
    private long firingInterval = DEFAULT_FIRING_INTERVAL;

    /**
     * Create a new gameObject to represent the players ship
     *
     * @param game The game in which the ship is being created
     * @param ref  The reference to the sprite to show for the ship
     * @param x    The initial x location of the player's ship
     * @param y    The initial y location of the player's ship
     */
    public ShipGameObject(Game game, String ref, int x, int y) {
        super(game, ref, x, y);
    }

    /**
     * Request that the ship move itself based on an elapsed ammount of
     * time
     *
     * @param delta The time that has elapsed since last move (ms)
     */
    public void move(long delta) {
        // if we're moving left and have reached the left hand side
        // of the screen, don't move
        if ((dx < 0) && (x < 10)) {
            return;
        }
        // if we're moving right and have reached the right hand side
        // of the screen, don't move
        if ((dx > 0) && (x > MAX_X - Game.SCREEN_EDGE_INNER_BUFFER)) {
            return;
        }

        super.move(delta);
    }

    /**
     * Notification that the player's ship has collided with something
     *
     * @param other The gameObject with which the ship has collided
     */
    public void collidedWith(GameObject other) {
        // if its an enemy, notify the game that the player
        // is dead
        if (other instanceof GOEnemy) {
            game.notifyDeath();
        }
    }

    public void moveStop(){
        setHorizontalMovement(0);
    }

    public void moveLeft(){
        setHorizontalMovement(-moveSpeed);
    }

    public void moveRight() {
        setHorizontalMovement(moveSpeed);
    }

    /**
     * Attempt to fire a bullet from the player. Its called "try"
     * since we must first check that the player can fire at this
     * point, i.e. has he/she waited long enough between bullets
     */
    public void tryToFire() {
        // check that we have waiting long enough to fire
        if (System.currentTimeMillis() - lastFire < firingInterval) {
            return;
        }

        // if we waited long enough, create the bullet gameObject, and record the time.
        lastFire = System.currentTimeMillis();
        GOBullet bullet = new GOBullet(game, GOBullet.SPRITES_BULLET_GIF, getX(), getY());
        bullet.adjustX(-bullet.getImageWidth()/2);
        bullet.adjustY(-bullet.getImageHeight());
        game.addTogameObjects(bullet);
        bullet = new GOBullet(game, GOBullet.SPRITES_BULLET_GIF, getX() + getImageWidth(), getY());
        bullet.adjustX(-bullet.getImageWidth()/2);
        bullet.adjustY(-bullet.getImageHeight());
        game.addTogameObjects(bullet);
    }
}