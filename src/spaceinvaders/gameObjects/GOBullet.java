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

    public void bulletHitsEnemy(GOEnemy enemy){
        // prevents double kills, if we've already hit something, don't collide
        if (!isUsed()) {
            enemy.takeDamage(1);
            uses--;
        }
    }

    public boolean isUsed(){
        return uses < 1;
    }
}