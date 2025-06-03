package com.tavuc.models.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;


public class Player extends Entity {

    private int playerId;
    private String username;
    private double accelleration;
    private double speed;
    private double direction;
    private double lastSentDirection;
    private Ellipse2D.Double playerBody;
    private Ellipse2D.Double playerLeftHand;
    private Ellipse2D.Double playerRightHand;
    private int lastSentX; 
    private int lastSentY; 
    private double lastSentDx;
    private double lastSentDy;
    private static final int PLAYER_BASE_WIDTH = 80; 
    private static final int PLAYER_BASE_HEIGHT = 80; 
    private static final int HAND_SIZE = 40; 
    private static final double MAX_SPEED = 5.0;
    private static final double ACCELERATION_RATE = 0.5;
    private static final double DECELERATION_RATE = 0.3;


    /**
     * Constructor for Player
     * @param playerId the unique ID of the player
     * @param username the username of the player
     */
    public Player(int playerId, String username) {
        super(50, 50, PLAYER_BASE_WIDTH, PLAYER_BASE_HEIGHT, 0.0, 100);
        this.playerId = playerId;
        this.username = username;
        this.accelleration = 0.0;
        this.speed = 0.0;
        this.direction = 0.0;
        this.lastSentX = (int)this.x;
        this.lastSentY = (int)this.y;
        this.lastSentDx = this.dx;
        this.lastSentDy = this.dy;
        this.lastSentDirection = 0.0;

        updatePlayerShapes();
    }

    /**
     * Constructor for Player in Ship Interior
     * @param x initial x position
     * @param y initial y position
     */
    public Player(int x, int y) {
        super(x, y, PLAYER_BASE_WIDTH, PLAYER_BASE_HEIGHT, 0.0, 100); 
        this.username = "Player"; 
        this.playerId = 0; 
        this.accelleration = 0.0;
        this.speed = 0.0;
        this.direction = 0.0; 
        updatePlayerShapes();
        //TODO: UPDATE SHIP INTERIOR TO USE NORMAL PLAYER
    }


    /**
     * Gets the player ID.
     * @return the player ID
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Gets the username of the player.
     * @return the username of the player
     */
    public String getUsername() {
        return username;
    }


    /**
     * Gets the acceleration input for the player.
     * @return the acceleration input
     */
    public double getAccelleration() {
        return accelleration;
    }

    /**
     * Gets the direction angle of the player in radians.
     * @return the direction angle in radians
     */
    public double getDirection() {
        return direction;
    }

    /**
     * Sets the x position of the player and updates the player shapes.
     * @param x the new x position
     */
    @Override
    public void setX(double x) {
        super.setX(x);
        updatePlayerShapes();
    }

    /**
     * Sets the y position of the player and updates the player shapes.
     * @param y the new y position
     */
    @Override
    public void setY(double y) {
        super.setY(y);
        updatePlayerShapes();
    }
    
    /**
     * Sets the health of the player. Overrides Entity's setHealth to handle double.
     * @param health the health of the player
     */
    public void setHealth(double health) { 
        super.setHealth((int)health); 
    }

    /**
     * Gets the health of the player as a double. Overrides Entity's getHealth.
     * @return the health of the player
     */
    @Override
    public int getHealth() { 
        return super.getHealth();
    }


    /**
     * Sets the acceleration input for the player.
     * @param accelleration the acceleration input
     */
    public void setAcceleration(double accelleration) {
        this.accelleration = accelleration;
    }

    /**
     * Sets the direction angle of the player in radians.
     * @param direction the direction angle in radians
     */
    public void setDirection(double direction) {
        this.direction = direction;
    }


    /**
     * Updates the shapes representing the player for rendering.
     * This includes the body and hands of the player.
     */
    private void updatePlayerShapes() {
        if (playerBody == null) playerBody = new Ellipse2D.Double();
        if (playerLeftHand == null) playerLeftHand = new Ellipse2D.Double();
        if (playerRightHand == null) playerRightHand = new Ellipse2D.Double();

        playerBody.setFrame(this.x, this.y, this.width, this.height);
        playerLeftHand.setFrame(this.x - HAND_SIZE / 2.0 + this.width / 4.0, this.y + this.height / 2.0 - HAND_SIZE / 2.0, HAND_SIZE, HAND_SIZE);
        playerRightHand.setFrame(this.x + this.width / 2.0 + HAND_SIZE / 2.0, this.y + this.height / 2.0 - HAND_SIZE / 2.0, HAND_SIZE, HAND_SIZE);
    }

    /**
     * Updates the player's state.
     * This method handles acceleration, deceleration, and movement based on the current speed and direction angle.
     * It also updates the player's position based on the calculated dx and dy.
     */
    @Override
    public void update() {
        if (accelleration > 0) {
            speed += ACCELERATION_RATE;
            if (speed > MAX_SPEED) {
                speed = MAX_SPEED;
            }
        } else {
            speed -= DECELERATION_RATE;
            if (speed < 0) {
                speed = 0;
            }
        }

        if (speed > 0) {
            this.dx = speed * Math.cos(direction);
            this.dy = speed * Math.sin(direction);
        } else {
            this.dx = 0;
            this.dy = 0;
        }
        
        move();
        updatePlayerShapes();
    }

    /**
     * Draws the player on the screen.
     * @param g2d the Graphics2D context to draw on
     * @param offsetX the world's x offset for camera view
     * @param offsetY the world's y offset for camera view
     */
    @Override
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        double screenX = this.x - offsetX;
        double screenY = this.y - offsetY;

        playerBody.setFrame(screenX, screenY, this.width, this.height);
        playerLeftHand.setFrame(screenX - HAND_SIZE / 2.0 + this.width / 4.0, screenY + this.height / 2.0 - HAND_SIZE / 2.0, HAND_SIZE, HAND_SIZE);
        playerRightHand.setFrame(screenX + this.width / 2.0 + HAND_SIZE / 2.0, screenY + this.height / 2.0 - HAND_SIZE / 2.0, HAND_SIZE, HAND_SIZE);
        
        g2d.setColor(Color.BLUE); 
        g2d.fill(playerBody);
        g2d.setColor(Color.CYAN); 
        g2d.fill(playerLeftHand);
        g2d.fill(playerRightHand);

        g2d.setColor(Color.WHITE);
        g2d.drawString(username, (float)screenX, (float)screenY - 5);

        // VISUAL DEBUGGING FOR HITBOX/HURTBOX
        // if (getHitbox() != null) {
        //     g2d.setColor(Color.RED);
        //     g2d.drawRect(
        //         (int)(getHitbox().x - offsetX), 
        //         (int)(getHitbox().y - offsetY), 
        //         getHitbox().width, 
        //         getHitbox().height
        //     );
        // }
        // if (getHurtbox() != null) {
        //     g2d.setColor(Color.GREEN); 
        //     g2d.drawRect(
        //         (int)(getHurtbox().x - offsetX), 
        //         (int)(getHurtbox().y - offsetY), 
        //         getHurtbox().width, 
        //         getHurtbox().height
        //     );
        // }
    }

    /**
     * Draws the player on the screen (for ship interior, no offset).
     * @param g2d the Graphics2D context to draw on
     */
    public void draw(Graphics2D g2d) {
        playerBody.setFrame(this.x, this.y, this.width, this.height);
        playerLeftHand.setFrame(this.x - HAND_SIZE / 2.0 + this.width / 4.0, this.y + this.height / 2.0 - HAND_SIZE / 2.0, HAND_SIZE, HAND_SIZE);
        playerRightHand.setFrame(this.x + this.width / 2.0 + HAND_SIZE / 2.0, this.y + this.height / 2.0 - HAND_SIZE / 2.0, HAND_SIZE, HAND_SIZE);
        
        g2d.setColor(Color.BLUE); 
        g2d.fill(playerBody);
        g2d.setColor(Color.CYAN); 
        g2d.fill(playerLeftHand);
        g2d.fill(playerRightHand);

        g2d.setColor(Color.WHITE);
        g2d.drawString(username, (float)this.x, (float)this.y - 5);
        //TODO: SET SHIP INTERIOR TO USE THE ONE FOR NORMAL PLAYER
    }

    /**
     * Gets the last sent x position.
     * @return the last sent x position
     */
    public int getLastSentX() { 
        return lastSentX; 
    }

    /**
     * Gets the last sent y position.
     * @return the last sent y position
     */
    public int getLastSentY() { 
        return lastSentY; 
    }

    /**
     * Gets the last sent dx.
     * @return the last sent dx
     */
    public double getLastSentDx() { 
        return lastSentDx; 
    }

    /**
     * Gets the last sent dy.
     * @return the last sent dy
     */
    public double getLastSentDy() { 
        return lastSentDy; 
    }

    /**
     * Gets the last sent direction angle.
     * @return the last sent direction angle
     */
    public double getlastSentDirection() { 
        return lastSentDirection; 
    }

    /**
     * Sets the last sent x position.
     * @param x the new last sent x position
     */
    public void setLastSentX(int x) { 
        this.lastSentX = x; 
    } 

    /**
     * Sets the last sent y position.
     * @param y the new last sent y position
     */
    public void setLastSentY(int y) { 
        this.lastSentY = y; 
    } 

    /**
     * Sets the last sent dx.
     * @param dx the new last sent dx
     */
    public void setLastSentDx(double dx) { 
        this.lastSentDx = dx; 
    }

    /**
     * Sets the last sent dy.
     * @param dy the new last sent dy
     */
    public void setLastSentDy(double dy) { 
        this.lastSentDy = dy; 
    }

    /**
     * Sets the last sent direction angle.
     * @param angle the new last sent direction angle
     */
    public void setlastSentDirection(double angle) { 
        this.lastSentDirection = angle; 
    }

}
