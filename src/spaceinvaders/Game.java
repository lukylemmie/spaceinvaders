package spaceinvaders;

import spaceinvaders.gameObjects.GOBullet;
import spaceinvaders.gameObjects.GOEnemy;
import spaceinvaders.gameObjects.GOShip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

/**
 * @author Andrew Lem
 */
public class Game extends Canvas {
    public static final int MAX_X = 800;
    public static final int MAX_Y = 600;
    public static final int SCREEN_EDGE_INNER_BUFFER = 50;
    public static final int SCREEN_EDGE_OUTER_BUFFER = 100;
    public static final String USER_INPUT_PROMPT = "Press any key to start, Press ESC to quit";

    /**
     * The strategy that allows us to use accelerate page flipping
     */
    private BufferStrategy strategy;
    private boolean gameRunning = true;

    private long lastLoopTime = System.currentTimeMillis();
    private ArrayList<GOEnemy> enemies = new ArrayList<>();
    private ArrayList<GOEnemy> removeEnemies = new ArrayList<>();
    private ArrayList<GOBullet> bullets = new ArrayList<>();
    private ArrayList<GOBullet> removeBullets = new ArrayList<>();
    private EnemyFormation enemyFormation;
    private GOShip ship;

    /**
     * The message to display which waiting for a key press
     */
    private String message = "";
    private boolean waitingForKeyPress = true;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean firePressed = false;
    /**
     * True if game logic needs to be applied this loop, normally as a result of a game event
     */

    /**
     * Construct our game and set it running.
     */
    public Game() {
        // create a frame to contain our game
        JFrame container = new JFrame("Space Invaders 101");

        // get hold the content of the frame and set up the resolution of the game
        JPanel panel = (JPanel) container.getContentPane();
        panel.setPreferredSize(new Dimension(MAX_X, MAX_Y));
        panel.setLayout(null);

        // setup our canvas size and put it into the content of the frame
        setBounds(0, 0, MAX_X, MAX_Y);
        panel.add(this);

        // Tell AWT not to bother repainting our canvas since we're
        // going to do that our self in accelerated mode
        setIgnoreRepaint(true);

        // finally make the window visible
        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        // add a listener to respond to the user closing the window. If they
        // do we'd like to exit the game
        container.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // add a key input system (defined below) to our canvas
        // so we can respond to key pressed
        addKeyListener(new KeyInputHandler());

        // request the focus so key events come to us
        requestFocus();

        // create the buffering strategy which will allow AWT
        // to manage our accelerated graphics
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // initialise the gameObjects in our game so there's something
        // to see at startup
        initGameObjects();
    }

    /**
     * The entry point into the game. We'll simply create an
     * instance of class which will start the display and game
     * loop.
     *
     * @param argv The arguments that are passed into our game
     */
    public static void main(String argv[]) {
        Game g = new Game();

        // Start the main game loop, note: this method will not
        // return until the game has finished running. Hence we are
        // using the actual main thread to run the game.
        g.gameLoop();
    }

    private void initGameObjects() {
        enemies.clear();
        bullets.clear();
        // create the player ship and place it roughly in the center of the screen
        ship = new GOShip(this, "sprites/ship.gif", (MAX_X - SCREEN_EDGE_INNER_BUFFER) / 2, MAX_Y - SCREEN_EDGE_INNER_BUFFER);
        enemyFormation = new EnemyFormation(this, 1);
    }

    /**
     * Start a fresh game, this should clear out any old data and
     * create a new set.
     */
    private void startGame() {
        // clear out any existing gameObjects and initialise a new set
        initGameObjects();

        // blank out any keyboard settings we might currently have
        leftPressed = false;
        rightPressed = false;
        firePressed = false;
    }

    public void gameLoop() {
        // keep looping round til the game ends
        while (gameRunning) {
            moveAndDrawGraphics();
            checkForCollisions();
            processUserInput();
            sleepForFPS();

        }
    }

    private void sleepForFPS() {
        // finally pause for a bit. Note: this should run us at about
        // 100 fps but on windows this might vary each loop due to
        // a bad implementation of timer
        try {
            Thread.sleep(10);
        } catch (Exception e) {
        }
    }

    private void moveAndDrawGraphics() {
        // work out how long its been since the last update, this will be used to calculate how far the gameObjects
        // should move this loop
        long delta = System.currentTimeMillis() - lastLoopTime;
        lastLoopTime = System.currentTimeMillis();

        // Get hold of a graphics context for the accelerated
        // surface and blank it out
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, MAX_X, MAX_Y);

        // cycle round asking each gameObject to move and draw itself
        if (waitingForKeyPress) {
            delta = 0;
        }
        ship.move(delta);
        ship.draw(g);
        for (GOEnemy enemy : enemies){
            enemy.move(delta);
            enemy.draw(g);
        }
        for (GOBullet bullet : bullets){
            bullet.move(delta);
            if (bullet.isOffScreen()) {
                removeBullets.add(bullet);
            }
            bullet.draw(g);
        }

        // if we're waiting for an "any key" press then draw the current message
        if (waitingForKeyPress) {
            g.setColor(Color.white);
            g.drawString(message, (MAX_X - g.getFontMetrics().stringWidth(message)) / 2, MAX_Y / 2 - SCREEN_EDGE_INNER_BUFFER);
            g.drawString(USER_INPUT_PROMPT,
                    (MAX_X - g.getFontMetrics().stringWidth("Press any key to start, Press ESC to quit")) / 2, MAX_Y / 2);
        }

        // finally, we've completed drawing so clear up the graphics and flip the buffer over
        g.dispose();
        strategy.show();
    }

    private void processUserInput() {
        // resolve the movement of the ship. First assume the ship
        // isn't moving. If either cursor key is pressed then
        // update the movement appropriately
        ship.moveStop();

        if ((leftPressed) && (!rightPressed)) {
            ship.moveLeft();
        } else if ((rightPressed) && (!leftPressed)) {
            ship.moveRight();
        }

        // if we're pressing fire, attempt to fire
        if (firePressed) {
            ship.tryToFire();
        }
    }

    private void checkForCollisions() {
        for (GOEnemy enemy : enemies){
            for (GOBullet bullet : bullets){
                if (bullet.collidesWith(enemy)) {
                    bullet.bulletHitsEnemy(enemy);
                }
                if (enemy.isDead()){
                    if (!removeEnemies.contains(enemy)) {
                        removeEnemies.add(enemy);
                    }
                }
                if (bullet.isUsed()){
                    if (!removeBullets.contains(bullet)) {
                        removeBullets.add(bullet);
                    }
                }
            }

            if(ship.collidesWith(enemy)) {
                notifyDeath();
            }
        }

        // remove any gameObject that has been marked for clear up
        enemies.removeAll(removeEnemies);
        for (GOEnemy enemy : removeEnemies){
            enemyFormation.remove(enemy);
            notifyEnemyKilled();
        }
        removeEnemies.clear();
        bullets.removeAll(removeBullets);
        removeBullets.clear();
    }

    public void notifyDeath() {
        message = "Oh no! They got you, try again?";
        waitingForKeyPress = true;
    }

    public void notifyWin() {
        message = "Well done! You Win!";
        waitingForKeyPress = true;
    }

    public void notifyEnemyKilled() {
        if (enemyFormation.isEmpty()) {
            notifyWin();
        }
        enemyFormation.increaseMovementSpeed();
    }

    public long getLastLoopTime() {
        return lastLoopTime;
    }

    public void addBullet(GOBullet bullet){
        bullets.add(bullet);
    }

    public void addEnemy(GOEnemy enemy){
        enemies.add(enemy);
    }

    private class KeyInputHandler extends KeyAdapter {
        public static final int ESC_KEY_VALUE = 27;
        private int pressCount = 1;

        public void keyPressed(KeyEvent e) {
            // if we're waiting for an "any key" typed then we don't
            // want to do anything with just a "press"
            if (waitingForKeyPress) {
                return;
            }


            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                firePressed = true;
            }
        }

        public void keyReleased(KeyEvent e) {
            // if we're waiting for an "any key" typed then we don't
            // want to do anything with just a "released"
            if (waitingForKeyPress) {
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                firePressed = false;
            }
        }

        /**
         * Notification from AWT that a key has been typed. Note that
         * typing a key means to both press and then release it.
         *
         * @param e The details of the key that was typed.
         */
        public void keyTyped(KeyEvent e) {
            // if we're waiting for a "any key" type then
            // check if we've received any recently. We may
            // have had a keyType() event from the user releasing
            // the shoot or move keys, hence the use of the "pressCount"
            // counter.
            if (waitingForKeyPress) {
                if (pressCount == 1) {
                    // since we've now received our key typed
                    // event we can mark it as such and start
                    // our new game
                    waitingForKeyPress = false;
                    startGame();
                    pressCount = 0;
                } else {
                    pressCount++;
                }
            }

            // if we hit escape, then quit the game
            if (e.getKeyChar() == ESC_KEY_VALUE) {
                System.exit(0);
            }
        }
    }
}
