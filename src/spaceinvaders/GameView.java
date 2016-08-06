package spaceinvaders;

import spaceinvaders.gameObjects.GOBullet;
import spaceinvaders.gameObjects.GOEnemy;
import spaceinvaders.gameObjects.GOShip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

/**
 * Created by Andrew on 06/08/2016.
 */
public class GameView extends Canvas {
    public static final String USER_INPUT_PROMPT = "Press any key to start, Press ESC to quit";

    /**
     * The strategy that allows us to use accelerate page flipping
     */
    private BufferStrategy strategy;
    private Game game;
    private UserInput userInput;

    /**
     * The message to display while waiting for a key press
     */
    private String message = "";

    public GameView(Game game, UserInput userInput){
        this.game = game;
        this.userInput = userInput;
        // create a frame to contain our game
        JFrame container = new JFrame("Space Invaders 101");

        // get hold the content of the frame and set up the resolution of the game
        JPanel panel = (JPanel) container.getContentPane();
        panel.setPreferredSize(new Dimension(Game.MAX_X, Game.MAX_Y));
        panel.setLayout(null);

        // setup our canvas size and put it into the content of the frame
        setBounds(0, 0, Game.MAX_X, Game.MAX_Y);
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
        addKeyListener(userInput.getKeyInputHandler());
        addMouseListener(userInput.getMouseInputHandler());

        // request the focus so key events come to us
        requestFocus();

        // create the buffering strategy which will allow AWT
        // to manage our accelerated graphics
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // initialise the gameObjects in our game so there's something
        // to see at startup
        game.initGameObjects();
    }



    public void moveAndDrawGraphics(long lastLoopTime, GOShip ship, ArrayList<GOEnemy> enemies, ArrayList<GOBullet> bullets) {
        // work out how long its been since the last update, this will be used to calculate how far the gameObjects
        // should move this loop
        long delta = System.currentTimeMillis() - lastLoopTime;
        lastLoopTime = System.currentTimeMillis();

        // Get hold of a graphics context for the accelerated
        // surface and blank it out
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, Game.MAX_X, Game.MAX_Y);

        // cycle round asking each gameObject to move and draw itself
        if (userInput.isWaitingForKeyPress()) {
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
            bullet.draw(g);
        }

        // if we're waiting for an "any key" press then draw the current message
        if (userInput.isWaitingForKeyPress()) {
            g.setColor(Color.white);
            g.drawString(message, (Game.MAX_X - g.getFontMetrics().stringWidth(message)) / 2, Game.MAX_Y / 2 - Game.SCREEN_EDGE_INNER_BUFFER);
            g.drawString(USER_INPUT_PROMPT,
                    (Game.MAX_X - g.getFontMetrics().stringWidth("Press any key to start, Press ESC to quit")) / 2, Game.MAX_Y / 2);
        }

        // finally, we've completed drawing so clear up the graphics and flip the buffer over
        g.dispose();
        strategy.show();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
