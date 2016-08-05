package spaceinvaders.gameObjects;

import spaceinvaders.sprites.Sprite;
import spaceinvaders.sprites.SpriteStore;

import java.awt.*;

/**
 * A gameObject represents any object that appears in the game. The
 * gameObject is responsible for movement and collision detection
 * based on a set of properties defined either by subclass or externally.
 *
 * @author Andrew Lem
 */
public abstract class GameObject {
    protected double x;
    protected double y;
    protected Sprite sprite;
    protected double dx;
    protected double dy;
    /**
     * The rectangle used by this gameObject during collision detection
     */
    private Rectangle me = new Rectangle();
    /**
     * The rectangle used for other gameObjects during collision detection
     */
    private Rectangle him = new Rectangle();

    /**
     * Construct a gameObject based on a sprite image and a location.
     *
     * @param ref The reference to the image to be displayed for this gameObject
     * @param x   The initial x location of this gameObject
     * @param y   The initial y location of this gameObject
     */
    public GameObject(String ref, int x, int y) {
        sprite = SpriteStore.get().getSprite(ref);
        this.x = x;
        this.y = y;
    }

    /**
     * Request that this gameObject move itself based on a certain amount
     * of time passing.
     *
     * @param delta The amount of time that has passed in milliseconds
     */
    public void move(long delta) {
        // update the location of the gameObject based on move speeds
        x += (delta * dx) / 1000;
        y += (delta * dy) / 1000;
    }

    public int getX() {
        return (int) x;
    }
    public int getY() {
        return (int) y;
    }
    public double getHorizontalMovement() {
        return dx;
    }
    public void setHorizontalMovement(double dx) {
        this.dx = dx;
    }
    public double getVerticalMovement() {
        return dy;
    }
    public void setVerticalMovement(double dy) {
        this.dy = dy;
    }

    /**
     * Draw this gameObject to the graphics context provided
     *
     * @param g The graphics context on which to draw
     */
    public void draw(Graphics g) {
        sprite.draw(g, (int) x, (int) y);
    }

    // TODO remove and rewrite logic
    public void doLogic() {
    }

    /**
     * Check if this gameObject collided with another.
     *
     * @param other The other gameObject to check collision against
     * @return True if the gameObjects collide with each other
     */
    public boolean collidesWith(GameObject other) {
        me.setBounds((int) x, (int) y, sprite.getWidth(), sprite.getHeight());
        him.setBounds((int) other.x, (int) other.y, other.sprite.getWidth(), other.sprite.getHeight());

        return me.intersects(him);
    }

    // TODO Refactor to boolean
    public abstract void collidedWith(GameObject other);

    public int getImageWidth(){
        return sprite.getWidth();
    }

    public int getImageHeight(){
        return sprite.getHeight();
    }

    public void adjustX(double shift) {
        x += shift;
    }

    public void adjustY(double shift) {
        y += shift;
    }
}