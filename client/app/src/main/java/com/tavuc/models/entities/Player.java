package com.tavuc.models.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;


/**
 * Represents a player character in the game world. 
 */
public class Player extends Entity {

    /**
     * The unique identifier for this player.
     */
    private int playerId;
    /**
     * The display name for this player.
     */
    private String username;
    /**
     * The current acceleration input, typically 1.0 for moving and 0.0 for stopping.
     */
    private double accelleration;
    /**
     * The current movement speed of the player.
     */
    private double speed;
    /**
     * The direction the player is facing, in radians.
     */
    private double direction;
    /**
     * The last direction value sent to the server, for network synchronization.
     */
    private double lastSentDirection;
    /**
     * The x and y components of the desired movement vector.
     */
    private double moveVecX = 0.0;
    private double moveVecY = 0.0;
    /**
     * The range of the player's melee attack.
     */
    private double attackRange = 60.0;
    /**
     * A flag to enable or disable the drawing of debug shapes like hitboxes.
     */
    private static final boolean DEBUG_DRAW_AREAS = false;
    /**
     * Geometric shapes used for rendering the player's body and hands.
     */
    private Ellipse2D.Double playerBody;
    private Ellipse2D.Double playerLeftHand;
    private Ellipse2D.Double playerRightHand;
    /**
     * The last known position and velocity sent to the server, used for state synchronization.
     */
    private int lastSentX; 
    private int lastSentY; 
    private double lastSentDx;
    private double lastSentDy;
    /**
     * Constants defining the player's physical and movement properties.
     */
    private static final int PLAYER_BASE_WIDTH = 120;
    private static final int PLAYER_BASE_HEIGHT = 120;
    private static final int HAND_SIZE = 60;
    private static final double MAX_SPEED = 5.0;
    private static final double ACCELERATION_RATE = 0.5;
    private static final double DECELERATION_RATE = 0.3;
    /**
     * A value from 0.0 to 1.0 controlling the intensity of the red "damage flash" effect.
     */
    private float damageEffect = 0f;


    /**
     * Constructs a new Player instance.
     * @param playerId The unique identifier for the player.
     * @param username The display name of the player.
     */
    public Player(int playerId, String username) {
        super(50, 50, PLAYER_BASE_WIDTH, PLAYER_BASE_HEIGHT, 0.0, 6);
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
     * Triggers the visual damage effect, causing the player to flash red.
     * This does not affect the player's health.
     */
    public void triggerDamageEffect() {
        damageEffect = 1.0f;
    }

    /**
     * Reduces the player's health by a given amount and triggers the damage flash effect.
     * @param amount The amount of damage to take.
     */
    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount);
        triggerDamageEffect();
    }

    /**
     * Updates the damage flash effect each frame, causing it to fade over time.
     */
    public void updateDamageEffect() {
        if (damageEffect > 0f) {
            damageEffect = Math.max(0f, damageEffect - 0.05f);
        }
    }

    /**
     * Gets the current intensity of the damage flash effect.
     * @return A float from 0.0 to 1.0.
     */
    public float getDamageEffect() {
        return damageEffect;
    }


    /**
     * Gets the melee attack range for this player.
     * @return The attack range in pixels.
     */
    public double getAttackRange() {
        return attackRange;
    }

    /**
     * Sets the melee attack range for this player.
     * @param attackRange The new attack range in pixels.
     */
    public void setAttackRange(double attackRange) {
        this.attackRange = attackRange;
    }



    /**
     * Gets the player's unique ID.
     * @return The player ID.
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Gets the username of the player.
     * @return The player's username.
     */
    public String getUsername() {
        return username;
    }


    /**
     * Gets the current acceleration input for the player.
     * @return The acceleration input value.
     */
    public double getAccelleration() {
        return accelleration;
    }

    /**
     * Gets the direction the player is facing.
     * @return The direction angle in radians.
     */
    public double getDirection() {
        return direction;
    }

    /** 
     * Sets the desired movement vector components. This vector determines the
     * direction of movement when the player has speed.
     * @param x The x-component of the movement vector.
     * @param y The y-component of the movement vector.
     */
    public void setMoveVector(double x, double y) {
        this.moveVecX = x;
        this.moveVecY = y;
    }

    /**
     * Gets the x-component of the movement vector.
     * @return The x-component of the movement vector.
     */
    public double getMoveVectorX() { return moveVecX; }

    /**
     * Gets the y-component of the movement vector.
     * @return The y-component of the movement vector.
     */
    public double getMoveVectorY() { return moveVecY; }

    /**
     * Sets the x-coordinate of the player and updates the renderable shapes.
     * @param x The new x-coordinate.
     */
    @Override
    public void setX(double x) {
        super.setX(x);
        updatePlayerShapes();
    }

    /**
     * Sets the y-coordinate of the player and updates the renderable shapes.
     * @param y The new y-coordinate.
     */
    @Override
    public void setY(double y) {
        super.setY(y);
        updatePlayerShapes();
    }
    
    /**
     * Sets the health of the player. This override handles the server's half-heart
     * health scale by flooring the value to an integer.
     * @param health The health value, typically in half-heart units.
     */
    public void setHealth(double health) {

        super.setHealth((int) Math.floor(health));
    }

    /**
     * Gets the health of the player.
     * @return The current health value as an integer.
     */
    @Override
    public int getHealth() { 
        return super.getHealth();
    }


    /**
     * Sets the acceleration input for the player.
     * @param accelleration The new acceleration input value.
     */
    public void setAcceleration(double accelleration) {
        this.accelleration = accelleration;
    }

    /**
     * Sets the direction the player is facing.
     * @param direction The new direction angle in radians.
     */
    public void setDirection(double direction) {
        this.direction = direction;
    }


    /**
     * Updates the geometric shapes used for rendering the player's body and hands
     * based on the player's current position and size.
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
     * Updates the player's state for a single frame. 
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
            double len = Math.sqrt(moveVecX * moveVecX + moveVecY * moveVecY);
            if (len > 0) {
                double normX = moveVecX / len;
                double normY = moveVecY / len;
                this.dx = speed * normX;
                this.dy = speed * normY;
            } else {
                this.dx = 0;
                this.dy = 0;
            }
        } else {
            this.dx = 0;
            this.dy = 0;
        }
        
        move();
        updatePlayerShapes();
        updateDamageEffect();
    }

    /**
     * Draws the player on the screen, including the body, hands, username,
     * facing direction indicator, and any active visual effects.
     * @param g2d The Graphics2D context to draw on.
     * @param offsetX The camera's horizontal offset in the world.
     * @param offsetY The camera's vertical offset in the world.
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

       

        if (damageEffect > 0f) {
            g2d.setColor(new Color(1f, 0f, 0f, damageEffect));
            g2d.fill(playerBody);
            g2d.fill(playerLeftHand);
            g2d.fill(playerRightHand);
        }
    }

    /**
     * Gets the last x-coordinate that was sent to the server.
     * @return The last sent x-coordinate.
     */
    public int getLastSentX() { 
        return lastSentX; 
    }

    /**
     * Gets the last y-coordinate that was sent to the server.
     * @return The last sent y-coordinate.
     */
    public int getLastSentY() { 
        return lastSentY; 
    }

    /**
     * Gets the last horizontal velocity that was sent to the server.
     * @return The last sent dx value.
     */
    public double getLastSentDx() { 
        return lastSentDx; 
    }

    /**
     * Gets the last vertical velocity that was sent to the server.
     * @return The last sent dy value.
     */
    public double getLastSentDy() { 
        return lastSentDy; 
    }

    /**
     * Gets the last direction angle that was sent to the server.
     * @return The last sent direction angle in radians.
     */
    public double getlastSentDirection() { 
        return lastSentDirection; 
    }

    /**
     * Sets the last x-coordinate that was sent to the server.
     * @param x The x-coordinate to store.
     */
    public void setLastSentX(int x) { 
        this.lastSentX = x; 
    } 

    /**
     * Sets the last y-coordinate that was sent to the server.
     * @param y The y-coordinate to store.
     */
    public void setLastSentY(int y) { 
        this.lastSentY = y; 
    } 

    /**
     * Sets the last horizontal velocity that was sent to the server.
     * @param dx The dx value to store.
     */
    public void setLastSentDx(double dx) { 
        this.lastSentDx = dx; 
    }

    /**
     * Sets the last vertical velocity that was sent to the server.
     * @param dy The dy value to store.
     */
    public void setLastSentDy(double dy) { 
        this.lastSentDy = dy; 
    }

    /**
     * Sets the last direction angle that was sent to the server.
     * @param angle The direction angle in radians to store.
     */
    public void setlastSentDirection(double angle) { 
        this.lastSentDirection = angle; 
    }

}