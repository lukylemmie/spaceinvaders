package spaceinvaders.gameObjects;

import spaceinvaders.Game;

/**
 * A gameObject representing a bullet fired by the player's ship
 *
 * @author Andrew Lem
 */
public class GOBullet extends GameObject {
    public static final int DEFAULT_BULLET_MOVE_SPEED = -300;
    public static final String SPRITES_BULLET_GIF = "sprites/bullet.gif";
    private double moveSpeed = DEFAULT_BULLET_MOVE_SPEED;
    private int uses = 1;

    /**
     * Create a new bullet from the player
     *
     * @param game   The game in which the bullet has been created
     * @param sprite The sprite representing this bullet
     * @param x      The initial x location of the bullet
     * @param y      The initial y location of the bullet
     */
    public GOBullet(Game game, String sprite, int x, int y) {
        super(game, sprite, x, y);

        dy = moveSpeed;
    }

    /**
     * Request that this bullet moved based on time elapsed
     *
     * @param delta The time that has elapsed since last move
     */
    public void move(long delta) {
        // proceed with normal move
        super.move(delta);

        // TODO move this logic to game
        // if bullet has moved off the screen then remove bullet
        if (isOffScreen()) {
            game.removeGameObject(this);
        }
    }

    /**
     * Notification that this bullet has collided with another
     * gameObject
     *
     * @parma other The other gameObject with which we've collided
     */
    public void collidedWith(GameObject other) {
        // prevents double kills, if we've already hit something,
        // don't collide
        if (uses < 1) {
            return;
        }

        // if we've hit an enemy, kill it!
        if (other instanceof GOEnemy) {
            // remove the affected gameObjects
            game.removeGameObject(this);
            game.removeGameObject(other);

            // notify the game that the enemy has been killed
            game.removeEnemy((GOEnemy) other);
            game.notifyEnemyKilled();
            uses--;
        }
    }
}