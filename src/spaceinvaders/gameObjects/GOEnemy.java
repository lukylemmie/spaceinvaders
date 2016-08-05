package spaceinvaders.gameObjects;

import spaceinvaders.EnemyFormation;
import spaceinvaders.Game;

/**
 * An gameObject which represents an enemy.
 *
 * @author Andrew Lem
 */
public class GOEnemy extends GameObject {
    public static final int DEFAULT_ENEMY_MOVE_SPEED = 75;
    public static final double DEFAULT_ENEMY_MOVE_SPEED_INCREASE = 1.03;

    private double moveSpeed = DEFAULT_ENEMY_MOVE_SPEED;
    private EnemyFormation enemyFormation;
    private int hp = 1;

    /**
     * Create a new enemy gameObject
     *
     * @param game The game in which this gameObject is being created
     * @param ref  The sprite which should be displayed for this enemy
     * @param x    The initial x location of this enemy
     * @param y    The initial y location of this enemy
     */
    public GOEnemy(Game game, String ref, int x, int y, EnemyFormation enemyFormation) {
        super(game, ref, x, y);

        dx = -moveSpeed;
        this.enemyFormation = enemyFormation;
    }

    /**
     * Request that this enemy moved based on time elapsed
     *
     * @param delta The time that has elapsed since last move
     */
    public void move(long delta) {
        // if enemy reaches edge of screen, enemyFormation advances and turns around
        if (((dx < 0) && (x < 10)) || ((dx > 0) && (x > Game.MAX_X - Game.SCREEN_EDGE_INNER_BUFFER))) {
            enemyFormation.advanceAndChangeDirection();
        }

        // proceed with normal move
        super.move(delta);
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

    public void advance() {
        dx = -dx;
        y += 10;

        if (y > Game.MAX_Y - Game.SCREEN_EDGE_INNER_BUFFER) {
            game.notifyDeath();
        }
    }

    public void takeDamage(int damage){
        hp -= damage;
    }

    public boolean isDead(){
        return hp <= 0;
    }
}