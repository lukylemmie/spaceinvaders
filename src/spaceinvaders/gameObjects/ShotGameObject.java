package spaceinvaders.gameObjects;

import spaceinvaders.Game;

/**
 * A gameObject representing a shot fired by the player's ship
 *
 * @author Andrew Lem
 */
public class ShotGameObject extends GameObject {
    public static final int DEFAULT_SHOT_MOVE_SPEED = -300;
    public static final String SPRITES_SHOT_GIF = "sprites/shot.gif";
    private double moveSpeed = DEFAULT_SHOT_MOVE_SPEED;
    private Game game;
    private boolean used = false;

    /**
     * Create a new shot from the player
     *
     * @param game   The game in which the shot has been created
     * @param sprite The sprite representing this shot
     * @param x      The initial x location of the shot
     * @param y      The initial y location of the shot
     */
    public ShotGameObject(Game game, String sprite, int x, int y) {
        super(sprite, x, y);

        this.game = game;

        dy = moveSpeed;
    }

    /**
     * Request that this shot moved based on time elapsed
     *
     * @param delta The time that has elapsed since last move
     */
    public void move(long delta) {
        // proceed with normal move
        super.move(delta);

        // if we shot off the screen, remove ourselves
        if (y < -100) {
            game.removeGameObject(this);
        }
    }

    /**
     * Notification that this shot has collided with another
     * entity
     *
     * @parma other The other entity with which we've collided
     */
    public void collidedWith(GameObject other) {
        // prevents double kills, if we've already hit something,
        // don't collide
        if (used) {
            return;
        }

        // if we've hit an alien, kill it!
        if (other instanceof AlienGameObject) {
            // remove the affected gameObjects
            game.removeGameObject(this);
            game.removeGameObject(other);

            // notify the game that the alien has been killed
            game.notifyAlienKilled();
            game.removeAlien((AlienGameObject) other);
            used = true;
        }
    }
}