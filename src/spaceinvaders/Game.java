package spaceinvaders;

import spaceinvaders.gameObjects.AlienGameObject;
import spaceinvaders.gameObjects.GameObject;
import spaceinvaders.gameObjects.ShipGameObject;

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
 * entities in the game asking them to move and then drawing them
 * in the appropriate place. With the help of an inner class it
 * will also allow the player to control the main ship.
 * <p>
 * As a mediator it will be informed when entities within our game
 * detect events (e.g. alien killed, played died) and will take
 * appropriate game actions.
 *
 * @author Original code base - Kevin Glass, refactors - Andrew Lem
 */
public class Game extends Canvas {
    public static final int MAX_X = 800;
    public static final int MAX_Y = 600;
    public static final int SCREEN_EDGE_BUFFER = 50;
    public static final int DEFAULT_ALIENS_PER_ROW = 12;
    public static final int DEFAULT_ALIENS_ROWS = 5;
    public static final int DEFAULT_ALIEN_LEFT_EDGE_X = 100;
    public static final int DEFAULT_ALIEN_GAP_X = 50;
    public static final int DEFAULT_ALIEN_GAP_Y = 30;
    public static final int DEFAULT_ALIEN_TOP_EDGE_Y = 50;
    public static final String USER_INPUT_PROMPT = "Press any key to start, Press ESC to quit";

    /**
     * The strategy that allows us to use accelerate page flipping
     */
    private BufferStrategy strategy;
    private boolean gameRunning = true;
    private ArrayList<GameObject> entities = new ArrayList<>();
    private ArrayList<AlienGameObject> aliens = new ArrayList<>();
    private ArrayList<GameObject> removeList = new ArrayList<>();
    private ShipGameObject ship;
    private int alienCount;

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

        // initialise the entities in our game so there's something
        // to see at startup
        initEntities();
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
     * Initialise the starting state of the entities (ship and aliens). Each
     * entity will be added to the overall list of entities in the game.
     */
    private void initEntities() {
        // create the player ship and place it roughly in the center of the screen
        ship = new ShipGameObject(this, "sprites/ship.gif", (MAX_X - SCREEN_EDGE_BUFFER) / 2, MAX_Y - SCREEN_EDGE_BUFFER);
        entities.add(ship);

        // create a block of aliens (5 rows, by 12 aliens, spaced evenly)
        alienCount = 0;
        aliens.clear();
        for (int row = 0; row < DEFAULT_ALIENS_ROWS; row++) {
            for (int x = 0; x < DEFAULT_ALIENS_PER_ROW; x++) {
                AlienGameObject alien = new AlienGameObject(this, "sprites/alien.gif",
                        DEFAULT_ALIEN_LEFT_EDGE_X + (x * DEFAULT_ALIEN_GAP_X),
                        DEFAULT_ALIEN_TOP_EDGE_Y + row * DEFAULT_ALIEN_GAP_Y);
                entities.add(alien);
                aliens.add(alien);
                alienCount++;
            }
        }
    }

    /**
     * Start a fresh game, this should clear out any old data and
     * create a new set.
     */
    private void startGame() {
        // clear out any existing entities and intialise a new set
        entities.clear();
        initEntities();

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
     * - Moving the game entities
     * - Drawing the screen contents (entities, text)
     * - Updating game events
     * - Checking Input
     * <p>
     */
    public void gameLoop() {
        long lastLoopTime = System.currentTimeMillis();

        // keep looping round til the game ends
        while (gameRunning) {
            // work out how long its been since the last update, this
            // will be used to calculate how far the entities should
            // move this loop
            long delta = System.currentTimeMillis() - lastLoopTime;
            lastLoopTime = System.currentTimeMillis();

            // Get hold of a graphics context for the accelerated
            // surface and blank it out
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.setColor(Color.black);
            g.fillRect(0, 0, MAX_X, MAX_Y);

            // cycle round asking each entity to move itself
            if (!waitingForKeyPress) {
                for (int i = 0; i < entities.size(); i++) {
                    GameObject gameObject = (GameObject) entities.get(i);

                    gameObject.move(delta);
                }
            }

            // cycle round drawing all the entities we have in the game
            for (int i = 0; i < entities.size(); i++) {
                GameObject gameObject = (GameObject) entities.get(i);

                gameObject.draw(g);
            }

            // brute force collisions, compare every entity against
            // every other entity. If any of them collide notify
            // both entities that the collision has occurred
            for (int p = 0; p < entities.size(); p++) {
                for (int s = p + 1; s < entities.size(); s++) {
                    GameObject me = (GameObject) entities.get(p);
                    GameObject him = (GameObject) entities.get(s);

                    if (me.collidesWith(him)) {
                        me.collidedWith(him);
                        him.collidedWith(me);
                    }
                }
            }

            // remove any entity that has been marked for clear up
            entities.removeAll(removeList);
            removeList.clear();

            // if a game event has indicated that game logic should
            // be resolved, cycle round every entity requesting that
            // their personal logic should be considered.
            if (logicRequiredThisLoop) {
                for (int i = 0; i < entities.size(); i++) {
                    GameObject gameObject = entities.get(i);
                    gameObject.doLogic();
                }

                logicRequiredThisLoop = false;
            }

            // if we're waiting for an "any key" press then draw the
            // current message
            if (waitingForKeyPress) {
                g.setColor(Color.white);
                g.drawString(message, (MAX_X - g.getFontMetrics().stringWidth(message)) / 2, MAX_Y / 2 - SCREEN_EDGE_BUFFER);
                g.drawString(USER_INPUT_PROMPT,
                        (MAX_X - g.getFontMetrics().stringWidth("Press any key to start, Press ESC to quit")) / 2, MAX_Y / 2);
            }

            // finally, we've completed drawing so clear up the graphics
            // and flip the buffer over
            g.dispose();
            strategy.show();

            // resolve the movement of the ship. First assume the ship
            // isn't moving. If either cursor key is pressed then
            // update the movement appropraitely
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
     * Notification from a game entity that the logic of the game
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
     * Notification that the player has won since all the aliens
     * are dead.
     */
    public void notifyWin() {
        message = "Well done! You Win!";
        waitingForKeyPress = true;
    }

    /**
     * Notification that an alien has been killed
     */
    public void notifyAlienKilled() {
        // reduce the alien count, if there are none left, the player has won!
        alienCount--;

        if (aliens.isEmpty()) {
            notifyWin();
        }

        // if there are still some aliens left then they all need to get faster, so
        // speed up all the existing aliens
        for(AlienGameObject alien : aliens){
            alien.increaseMovementSpeed();
        }
    }

    public void addToEntities(GameObject gameObject) {
        entities.add(gameObject);
    }

    public void removeAlien(AlienGameObject alien) {
        aliens.remove(alien);
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
