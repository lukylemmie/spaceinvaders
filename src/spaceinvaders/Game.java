package spaceinvaders;

import spaceinvaders.gameObjects.GOBullet;
import spaceinvaders.gameObjects.GOEnemy;
import spaceinvaders.gameObjects.GameObject;
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
 * The main hook of our game. This class with both act as a manager
 * for the display and central mediator for the game logic.
 * <p>
 * Display management will consist of a loop that cycles round all
 * gameObjects in the game asking them to move and then drawing them
 * in the appropriate place. With the help of an inner class it
 * will also allow the player to control the main ship.
 * <p>
 * As a mediator it will be informed when gameObjects within our game
 * detect events (e.g. enemy killed, played died) and will take
 * appropriate game actions.
 *
 * @author Original code base - Kevin Glass, refactors - Andrew Lem
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
    private ArrayList<GameObject> gameObjects = new ArrayList<>();
    private ArrayList<GameObject> removeList = new ArrayList<>();
    private ArrayList<GOBullet> bullets = new ArrayList<>();
    private ArrayList<GOEnemy> enemies = new ArrayList<>();
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
    private boolean logicRequiredThisLoop = false;

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

    /**
     * Initialise the starting state of the gameObjects (ship and enemies). Each
     * gameObject will be added to the overall list of gameObjects in the game.
     */
    private void initGameObjects() {
        // create the player ship and place it roughly in the center of the screen
        ship = new GOShip(this, "sprites/ship.gif", (MAX_X - SCREEN_EDGE_INNER_BUFFER) / 2, MAX_Y - SCREEN_EDGE_INNER_BUFFER);
        gameObjects.add(ship);
        enemyFormation = new EnemyFormation(this, 1);
    }

    /**
     * Start a fresh game, this should clear out any old data and
     * create a new set.
     */
    private void startGame() {
        // clear out any existing gameObjects and intialise a new set
        gameObjects.clear();
        initGameObjects();

        // blank out any keyboard settings we might currently have
        leftPressed = false;
        rightPressed = false;
        firePressed = false;
    }

    /**
     * The main game loop. This loop is running during all game
     * play as is responsible for the following activities:
     * <p>
     * - Working out the speed of the game loop to update moves
     * - Moving the game gameObjects
     * - Drawing the screen contents (gameObjects, text)
     * - Updating game events
     * - Checking Input
     * <p>
     */
    public void gameLoop() {
        // keep looping round til the game ends
        while (gameRunning) {
            // work out how long its been since the last update, this
            // will be used to calculate how far the gameObjects should
            // move this loop
            long delta = System.currentTimeMillis() - lastLoopTime;
            lastLoopTime = System.currentTimeMillis();

            // Get hold of a graphics context for the accelerated
            // surface and blank it out
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.setColor(Color.black);
            g.fillRect(0, 0, MAX_X, MAX_Y);

            // cycle round asking each gameObject to move and draw itself
            if (!waitingForKeyPress) {
                for (GameObject gameObject : gameObjects){
                    gameObject.move(delta);
                }
            }

            for (GameObject gameObject : gameObjects){
                gameObject.draw(g);
            }

            // brute force collisions, compare every gameObject against
            // every other gameObject. If any of them collide notify
            // both gameObjects that the collision has occurred
            for (int p = 0; p < gameObjects.size(); p++) {
                for (int s = p + 1; s < gameObjects.size(); s++) {
                    GameObject me = gameObjects.get(p);
                    GameObject him = gameObjects.get(s);

                    if (me.collidesWith(him)) {
                        me.collidedWith(him);
                        him.collidedWith(me);
                    }
                }
            }


//            for (GOEnemy enemy : enemies){
//                for (GOBullet bullet : bullets){
//                    if(enemy.collidesWith(bullet)) {
//                        enemy.collidedWith(bullet);
//                        bullet.collidedWith(enemy);
//                    }
//                }
//
//                if(enemy.collidesWith(ship)) {
//                    enemy.collidedWith(ship);
//                    ship.collidesWith(enemy);
//                }
//            }

            // remove any gameObject that has been marked for clear up
            gameObjects.removeAll(removeList);
            removeList.clear();

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

            // finally pause for a bit. Note: this should run us at about
            // 100 fps but on windows this might vary each loop due to
            // a bad implementation of timer
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Notification from a game gameObject that the logic of the game
     * should be run at the next opportunity (normally as a result of some
     * game event)
     */
    public void updateLogic() {
        logicRequiredThisLoop = true;
    }

    /**
     * Remove an gameObject from the game. The gameObject removed will
     * no longer move or be drawn.
     *
     * @param gameObject The gameObject that should be removed
     */
    public void removeGameObject(GameObject gameObject) {
        removeList.add(gameObject);
    }

    /**
     * Notification that the player has died.
     */
    public void notifyDeath() {
        message = "Oh no! They got you, try again?";
        waitingForKeyPress = true;
    }

    /**
     * Notification that the player has won since all the enemies
     * are dead.
     */
    public void notifyWin() {
        message = "Well done! You Win!";
        waitingForKeyPress = true;
    }

    /**
     * Notification that an enemy has been killed
     */
    public void notifyEnemyKilled() {
        if (enemyFormation.isEmpty()) {
            notifyWin();
        }
        enemyFormation.increaseMovementSpeed();
    }

    public void addToGameObjects(GameObject gameObject) {
        gameObjects.add(gameObject);
    }

    public void removeEnemy(GOEnemy enemy) {
        enemyFormation.remove(enemy);
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

    /**
     * A class to handle keyboard input from the user. The class
     * handles both dynamic input during game play, i.e. left/right
     * and shoot, and more static type input (i.e. press any key to
     * continue)
     * <p>
     * This has been implemented as an inner class more through
     * habit then anything else. Its perfectly normal to implement
     * this as separate class if slight less convenient.
     *
     * @author Kevin Glass
     */
    private class KeyInputHandler extends KeyAdapter {
        public static final int ESC_KEY_VALUE = 27;
        /**
         * The number of key presses we've had while waiting for an "any key" press
         */
        private int pressCount = 1;

        /**
         * Notification from AWT that a key has been pressed. Note that
         * a key being pressed is equal to being pushed down but *NOT*
         * released. Thats where keyTyped() comes in.
         *
         * @param e The details of the key that was pressed
         */
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

        /**
         * Notification from AWT that a key has been released.
         *
         * @param e The details of the key that was released
         */
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
