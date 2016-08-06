package spaceinvaders;

import java.awt.event.*;

/**
 * Created by Andrew on 06/08/2016.
 */
public class UserInput {
    private boolean waitingForKeyPress = true;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean firePressed = false;
    private Game game;
    private KeyInputHandler keyInputHandler;
    private MouseInputHandler mouseInputHandler;

    public UserInput(Game game){
        this.game = game;
        this.keyInputHandler = new KeyInputHandler();
        this.mouseInputHandler = new MouseInputHandler();
    }

    public void reset(){
        waitingForKeyPress = true;
        clearPressed();
    }

    public void clearPressed(){
        leftPressed = false;
        rightPressed = false;
        firePressed = false;
    }

    public boolean isWaitingForKeyPress() {
        return waitingForKeyPress;
    }

    public KeyInputHandler getKeyInputHandler() {
        return keyInputHandler;
    }

    public MouseInputHandler getMouseInputHandler() {
        return mouseInputHandler;
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public boolean isFirePressed() {
        return firePressed;
    }

    public void waitForKeyPress() {
        waitingForKeyPress = true;
    }

    public class KeyInputHandler extends KeyAdapter {
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
                    game.startGame();
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

    public class MouseInputHandler extends MouseAdapter {

        public void mouseMoved(MouseEvent e) {
            System.out.println("X : " + e.getX());
            System.out.println("Y : " + e.getY());
        }

        public void mouseDragged(MouseEvent e) {
            //do something
        }

    }
}
