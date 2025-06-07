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
    private double moveVecX = 0.0;
    private double moveVecY = 0.0;
    // Attack radius for melee actions - increased to complement dash mechanic
    private double attackRange = 60.0;
    // Toggle drawing of hitboxes and attack ranges for debugging
    private static final boolean DEBUG_DRAW_AREAS = false;
    private Ellipse2D.Double playerBody;
    private Ellipse2D.Double playerLeftHand;
    private Ellipse2D.Double playerRightHand;
    private int lastSentX; 
    private int lastSentY; 
    private double lastSentDx;
    private double lastSentDy;
    private static final int PLAYER_BASE_WIDTH = 120;
    private static final int PLAYER_BASE_HEIGHT = 120;
    private static final int HAND_SIZE = 60;
    private static final double MAX_SPEED = 5.0;
    private static final double ACCELERATION_RATE = 0.5;
    private static final double DECELERATION_RATE = 0.3;
    // Visual damage flash strength (1.0 = fully visible, 0 = no effect)
    private float damageEffect = 0f;


    /**
     * Constructor for Player
     * @param playerId the unique ID of the player
     * @param username the username of the player
     */
    public Player(int playerId, String username) {
        // Health is measured in half-hearts (0-6)
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
     * Triggers the damage flash effect without altering health.
     */
    public void triggerDamageEffect() {
        damageEffect = 1.0f;
    }

    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount);
        triggerDamageEffect();
    }

    /**
     * Updates and fades the damage flash each frame.
     */
    public void updateDamageEffect() {
        if (damageEffect > 0f) {
            damageEffect = Math.max(0f, damageEffect - 0.05f);
        }
    }

    public float getDamageEffect() {
        return damageEffect;
    }


    /**
     * Gets the melee attack range for this player.
     */
    public double getAttackRange() {
        return attackRange;
    }

    /**
     * Sets the melee attack range for this player.
     */
    public void setAttackRange(double attackRange) {
        this.attackRange = attackRange;
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

    /** Sets the desired movement vector components. */
    public void setMoveVector(double x, double y) {
        this.moveVecX = x;
        this.moveVecY = y;
    }

    public double getMoveVectorX() { return moveVecX; }

    public double getMoveVectorY() { return moveVecY; }

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
     * Sets the health of the player. Overrides Entity's setHealth to handle
     * the server's half-heart health scale directly.
     * @param health the health of the player in half-heart units
     */
    public void setHealth(double health) {
        // Server sends fractional half-heart values (e.g. 5.5). If we round
        // to the nearest integer the health bar would only update every other
        // hit. Cast down instead so each half-heart of damage is reflected
        // immediately on the client.
        super.setHealth((int) Math.floor(health));
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

        // Draw facing direction arrow
        double centerX = screenX + this.width / 2.0;
        double centerY = screenY + this.height / 2.0;
        int arrowLength = this.width / 2;
        int endX = (int)(centerX + Math.cos(direction) * arrowLength);
        int endY = (int)(centerY + Math.sin(direction) * arrowLength);
        g2d.setColor(Color.YELLOW);
        g2d.drawLine((int)centerX, (int)centerY, endX, endY);
        int ah = 6;
        int hx1 = (int)(endX - Math.cos(direction - Math.PI/6) * ah);
        int hy1 = (int)(endY - Math.sin(direction - Math.PI/6) * ah);
        int hx2 = (int)(endX - Math.cos(direction + Math.PI/6) * ah);
        int hy2 = (int)(endY - Math.sin(direction + Math.PI/6) * ah);
        g2d.drawLine(endX, endY, hx1, hy1);
        g2d.drawLine(endX, endY, hx2, hy2);

        if (damageEffect > 0f) {
            g2d.setColor(new Color(1f, 0f, 0f, damageEffect));
            g2d.fill(playerBody);
            g2d.fill(playerLeftHand);
            g2d.fill(playerRightHand);
        }



        if (DEBUG_DRAW_AREAS) {
            if (getHitbox() != null) {
                g2d.setColor(Color.RED);
                g2d.drawRect(
                        (int)(getHitbox().x - offsetX),
                        (int)(getHitbox().y - offsetY),
                        getHitbox().width,
                        getHitbox().height
                );
            }
            if (getHurtbox() != null) {
                g2d.setColor(Color.GREEN);
                g2d.drawRect(
                        (int)(getHurtbox().x - offsetX),
                        (int)(getHurtbox().y - offsetY),
                        getHurtbox().width,
                        getHurtbox().height
                );
            }
            g2d.setColor(Color.YELLOW);
            g2d.drawOval(
                    (int)(screenX - attackRange),
                    (int)(screenY - attackRange),
                    (int)(this.width + attackRange * 2),
                    (int)(this.height + attackRange * 2)
            );
        }
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
