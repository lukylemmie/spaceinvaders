package spaceinvaders;

import spaceinvaders.gameObjects.GOBullet;
import spaceinvaders.gameObjects.GOEnemy;
import spaceinvaders.gameObjects.GOShip;

import java.util.ArrayList;

/**
 * @author Andrew Lem
 */
public class Game {
    public static final int MAX_X = 800;
    public static final int MAX_Y = 600;
    public static final int SCREEN_EDGE_INNER_BUFFER = 50;
    public static final int SCREEN_EDGE_OUTER_BUFFER = 100;

    private boolean gameRunning = true;
    private long lastLoopTime = System.currentTimeMillis();
    private ArrayList<GOEnemy> enemies = new ArrayList<>();
    private ArrayList<GOEnemy> removeEnemies = new ArrayList<>();
    private ArrayList<GOBullet> bullets = new ArrayList<>();
    private ArrayList<GOBullet> removeBullets = new ArrayList<>();
    private EnemyFormation enemyFormation;
    private GOShip ship;
    private UserInput userInput;
    private GameView gameView;
    private boolean mouseControls = true;
    private boolean keyboardControls = false;

    /**
     * Construct our game and set it running.
     */
    public Game() {
        userInput = new UserInput(this);
        gameView = new GameView(this, userInput);
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

    public void initGameObjects() {
        enemies.clear();
        bullets.clear();
        // create the player ship and place it roughly in the center of the screen
        ship = new GOShip(this, "sprites/ship.gif", MAX_X / 2, MAX_Y - SCREEN_EDGE_INNER_BUFFER);
        enemyFormation = new EnemyFormation(this, 1);
    }

    /**
     * Start a fresh game, this should clear out any old data and
     * create a new set.
     */
    public void startGame() {
        // clear out any existing gameObjects and initialise a new set
        initGameObjects();

        // blank out any keyboard settings we might currently have
        userInput.clearPressed();
    }

    public void gameLoop() {
        // keep looping round til the game ends
        while (gameRunning) {
            moveGameObjects();
            gameView.drawGameObjects(ship, enemies, bullets);
            checkForCollisions();
            processUserInput();
            sleepForFPS();

        }
    }

    public void moveGameObjects(){
        // work out how long its been since the last update, this will be used to calculate how far the gameObjects
        // should move this loop
        long delta = System.currentTimeMillis() - lastLoopTime;
        lastLoopTime = System.currentTimeMillis();

        if (!userInput.isWaitingForKeyPress()) {
            ship.move(delta);
            for (GOEnemy enemy : enemies) {
                enemy.move(delta);
            }
            for (GOBullet bullet : bullets) {
                bullet.move(delta);
                if (bullet.isOffScreen()) {
                    removeBullets.add(bullet);
                }
            }
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

    private void processUserInput() {
        // resolve the movement of the ship. First assume the ship
        // isn't moving. If either cursor key is pressed then
        // update the movement appropriately
        ship.moveStop();


        if (mouseControls) {
            if (userInput.getMouseX() < ship.getX() - 1) {
                ship.moveLeft();
            } else if (userInput.getMouseX() > ship.getX() + 1) {
                ship.moveRight();
            }

            // if we're pressing fire, attempt to fire
            if (userInput.isMouseClick()) {
                ship.tryToFire();
            }
        }

        if (keyboardControls) {
            if ((userInput.isLeftPressed()) && (!userInput.isRightPressed())) {
                ship.moveLeft();
            } else if ((userInput.isRightPressed()) && (!userInput.isLeftPressed())) {
                ship.moveRight();
            }

            // if we're pressing fire, attempt to fire
            if (userInput.isFirePressed()) {
                ship.tryToFire();
            }
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
        gameView.setMessage("Oh no! They got you, try again?");
        userInput.waitForKeyPress();
    }

    public void notifyWin() {
        gameView.setMessage("Well done! You Win!");
        userInput.waitForKeyPress();
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
}
