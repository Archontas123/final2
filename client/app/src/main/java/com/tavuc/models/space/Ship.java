package com.tavuc.models.space;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;

import com.tavuc.ecs.ComponentContainer;
import com.tavuc.ecs.components.*;

/**
 * Represents a player-controlled ship in the space enviormenet.
 */
public class Ship {

    /** An array of images for rendering the ship with different levels of engine boost. */
    private BufferedImage[] shipImages;
    /** The number of different boost animation frames/levels. */
    private static final int NUM_BOOST_LEVELS = 5;

    /** The ship's world coordinates. */
    private double x, y;
    /** The ship's velocity components. */
    private double dx, dy;
    /** The dimensions of the ship's sprite. */
    private int width, height;
    /** The ship's current rotation in radians. */
    private double angle;
    /** The mass of the ship, used in physics calculations. */
    private double mass;

    /** The current rotational input (-1 for left, 0 for none, 1 for right). */
    private double rotationInput;
    /** A flag indicating if the main engine is currently active. */
    private boolean thrusting;
    
    /** A container for this ship's components (e.g., HealthComponent, ShieldComponent). */
    private ComponentContainer components;
    /** A simple rectangular boundary for collision detection. */
    private Rectangle collisionBounds;
    /** A flag indicating if the ship has been destroyed. */
    private boolean destroyed = false;

    /** The rate of rotation in radians per update cycle. */
    private static final double ROTATION_AMOUNT = Math.toRadians(5.0);
    /** The force applied by the main thruster. */
    private static final double THRUST_FORCE = 15.0;
    /** The maximum speed the ship can reach. */
    private static final double MAX_SPEED = 30.0;
    /** The factor by which velocity is reduced each frame when not thrusting (space friction). */
    private static final double DAMPING_FACTOR = 0.985;
    /** A factor that helps the ship's velocity align with its orientation while turning. */
    private static final double STEERING_ASSIST_FACTOR = 0.08;
    
    /** The current visual intensity of the shield bubble. */
    private float shieldVisualStrength = 0.0f;
    /** The current intensity of the "flash" effect when the shield is hit. */
    private float shieldHitEffect = 0.0f;
    /** The system time when the ship last took damage, used for shield recharge logic. */
    private long lastDamageTime = 0;

    /**
     * Constructs a new Ship at a specified starting position.
     * @param centerX The starting x-coordinate for the center of the ship.
     * @param centerY The starting y-coordinate for the center of the ship.
     */
    public Ship(double centerX, double centerY) {
        this.x = centerX;
        this.y = centerY;
        this.width = 128;
        this.height = 128;
        this.dx = 0;
        this.dy = 0;
        this.angle = 0.0;
        this.mass = 100.0;
        this.rotationInput = 0;
        this.thrusting = false;
        this.components = new ComponentContainer();
        this.collisionBounds = new Rectangle((int)x, (int)y, width, height);
        
        components.addComponent(new HealthComponent(100f));
        components.addComponent(new ShieldComponent(100f, 10f, 3f)); 
        
        loadImages();
    }
    
    /**
     * Gets the component container for this ship.
     * @return The {@link ComponentContainer} holding this ship's components.
     */
    public ComponentContainer getComponents() {
        return components;
    }
    
    /**
     * A convenience method to get the ship's current health.
     * @return The current health, or 0 if the HealthComponent is missing.
     */
    public float getHealth() {
        HealthComponent health = components.getComponent(HealthComponent.class);
        return health != null ? health.getHealth() : 0f;
    }
    
    /**
     * A convenience method to get the ship's current health as a percentage.
     * @return The current health percentage, or 0 if the HealthComponent is missing.
     */
    public float getHealthPercentage() {
        HealthComponent health = components.getComponent(HealthComponent.class);
        return health != null ? health.getHealthPercentage() : 0f;
    }
    
    /**
     * A convenience method to get the ship's current shield strength.
     * @return The current shield strength, or 0 if the ShieldComponent is missing.
     */
    public float getShield() {
        ShieldComponent shield = components.getComponent(ShieldComponent.class);
        return shield != null ? shield.getShield() : 0f;
    }
    
    /**
     * A convenience method to get the ship's current shield strength as a percentage.
     * @return The current shield percentage, or 0 if the ShieldComponent is missing.
     */
    public float getShieldPercentage() {
        ShieldComponent shield = components.getComponent(ShieldComponent.class);
        return shield != null ? shield.getShieldPercentage() : 0f;
    }
    
    /**
     * Checks if the ship is destroyed.
     * @return {@code true} if the ship is marked as destroyed or its health is zero or less.
     */
    public boolean isDestroyed() {
        return destroyed || getHealth() <= 0;
    }
    
    /**
     * Sets the destroyed state of the ship.
     * @param destroyed {@code true} to mark the ship as destroyed, {@code false} otherwise.
     */
    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    /**
     * Applies a specified amount of damage to the ship.
     * @param amount The amount of damage to apply.
     */
    public void takeDamage(double amount) {
        ShieldComponent shield = components.getComponent(ShieldComponent.class);
        HealthComponent health = components.getComponent(HealthComponent.class);

        lastDamageTime = System.currentTimeMillis();

        shieldHitEffect = 1.0f;

        if (shield != null && shield.hasShield()) {
            double shieldDamage = Math.min(amount, shield.getShield());
            amount -= shieldDamage;
            shield.takeDamage((float) shieldDamage);
        }

        if (amount > 0 && health != null) {
            health.takeDamage((float) amount);

            if (health.getHealth() <= 0) {
                setDestroyed(true);
            }
        }
    }

    /**
     * Triggers the visual hit effect on the shield without modifying health or shield values.
     */
    public void triggerHitEffect() {
        lastDamageTime = System.currentTimeMillis();
        shieldHitEffect = 1.0f;
    }

    /**
     * Updates the ship's health and max health from server data to ensure consistency.
     * @param currentHealth The authoritative current health value from the server.
     * @param maxHealth The authoritative maximum health value from the server.
     */
    public void updateHealthFromServer(float currentHealth, float maxHealth) {
        HealthComponent health = components.getComponent(HealthComponent.class);
        if (health != null) {
            health.setMaxHealth(maxHealth);
            health.setCurrentHealth(currentHealth);
        }
        
        if (currentHealth <= 0) {
            setDestroyed(true);
        }
    }
    /**
     * Updates the ship's state for a single frame.
     * @param deltaTime The time elapsed since the last update, in seconds.
     */
    public void update(double deltaTime) {
        updatePosition();
    }

    /**
     * Sets the ship's x-coordinate.
     * @param x The new x-coordinate.
     */
    public void setX(double x) {
        this.x = x;
        updateCollisionBounds();
    }

    /**
     * Sets the ship's y-coordinate.
     * @param y The new y-coordinate.
     */
    public void setY(double y) {
        this.y = y;
        updateCollisionBounds();
    }
    
    /**
     * A private helper method to update the position of the collision bounds rectangle.
     */
    private void updateCollisionBounds() {
        this.collisionBounds.setLocation((int)x, (int)y);
    }

    /**
     * Sets the ship's angle of rotation.
     * @param angle The new angle in radians.
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * Sets the ship's velocity on the x-axis.
     * @param dx The new x-velocity.
     */
    public void setDx(double dx) {
        this.dx = dx;
    }

    /**
     * Sets the ship's velocity on the y-axis.
     * @param dy The new y-velocity.
     */
    public void setDy(double dy) {
        this.dy = dy;
    }

    /**
     * Checks if the ship's thrusters are active.
     * @return {@code true} if thrusting, {@code false} otherwise.
     */
    public boolean isThrusting() {
        return thrusting;
    }
    
    /**
     * Gets the collision bounds of the ship.
     * @return A {@link Rectangle} representing the ship's collision area.
     */
    public Rectangle getCollisionBounds() {
        return collisionBounds;
    }

    /**
     * A private helper method to load the ship's sprite images from resources.
     */
    private void loadImages() {
        shipImages = new BufferedImage[NUM_BOOST_LEVELS + 1];
        String basePath = "assets/ship/exterior/";
        try {
            String noBoostPath = basePath + "ship_no_boost.png";
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(noBoostPath)) {
                if (is == null) throw new IOException("Resource not found: " + noBoostPath);
                shipImages[0] = ImageIO.read(is);
            }

            for (int i = 1; i <= NUM_BOOST_LEVELS; i++) {
                String boostPath = basePath + "ship_boost_" + i + ".png";
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(boostPath)) {
                    if (is == null) throw new IOException("Resource not found: " + boostPath);
                    shipImages[i] = ImageIO.read(is);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading ship images: " + e.getMessage());
            e.printStackTrace();
            for (int i = 0; i < shipImages.length; i++) {
                shipImages[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = shipImages[i].createGraphics();
                g.setColor(Color.RED);
                g.fillRect(0, 0, width, height);
                g.setColor(Color.WHITE);
                g.drawString("ERR " + i, 5, height / 2);
                g.dispose();
            }
        }
    }

    /**
     * Sets the rotation input for the ship.
     * @param rotationInput A value of -1 for left, 0 for no rotation, or 1 for right.
     */
    public void setRotationInput(double rotationInput) {
        this.rotationInput = rotationInput;
    }

    /**
     * Sets whether the ship's main thruster is active.
     * @param thrusting {@code true} if thrusting, {@code false} otherwise.
     */
    public void setThrusting(boolean thrusting) {
        this.thrusting = thrusting;
    }

    /**
     * Gets the x-coordinate of the ship's center.
     * @return The x-coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the ship's center.
     * @return The y-coordinate.
     */
    public double getY() {
        return y;
    }
    
    /**
     * Gets the width of the ship.
     * @return The width.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Gets the height of the ship.
     * @return The height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the current angle of the ship.
     * @return The angle in radians.
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Gets the current velocity of the ship on the x-axis.
     * @return The dx value.
     */
    public double getDx() {
        return dx;
    }

    /**
     * Gets the current velocity of the ship on the y-axis.
     * @return The dy value.
     */
    public double getDy() {
        return dy;
    }

    /**
     * Updates the position and orientation of the ship based on its current
     * velocity, rotation input, and physics properties.
     */
    public void updatePosition() {
        if (isDestroyed()) {
            return; 
        }
        
        this.angle += this.rotationInput * ROTATION_AMOUNT;
        this.angle %= (2 * Math.PI);
        if (this.angle < 0) {
            this.angle += (2 * Math.PI);
        }

        if (this.thrusting) {
            double accelerationX = (Math.sin(this.angle) * THRUST_FORCE) / this.mass;
            double accelerationY = (-Math.cos(this.angle) * THRUST_FORCE) / this.mass; 
            this.dx += accelerationX;
            this.dy += accelerationY;
        } else {
            this.dx *= DAMPING_FACTOR;
            this.dy *= DAMPING_FACTOR;
        }

        if (this.rotationInput != 0 && (this.dx != 0 || this.dy != 0)) {
            double currentSpeed = Math.sqrt(this.dx * this.dx + this.dy * this.dy);
            if (currentSpeed > 0.01) { 
                double targetDx = Math.sin(this.angle) * currentSpeed;
                double targetDy = -Math.cos(this.angle) * currentSpeed; 

                this.dx = this.dx * (1 - STEERING_ASSIST_FACTOR) + targetDx * STEERING_ASSIST_FACTOR;
                this.dy = this.dy * (1 - STEERING_ASSIST_FACTOR) + targetDy * STEERING_ASSIST_FACTOR;
            }
        }
        
        double currentSpeed = Math.sqrt(this.dx * this.dx + this.dy * this.dy);
        if (currentSpeed > MAX_SPEED) {
            this.dx = (this.dx / currentSpeed) * MAX_SPEED;
            this.dy = (this.dy / currentSpeed) * MAX_SPEED;
        }

        if (currentSpeed < 0.01 && !this.thrusting) { 
            this.dx = 0;
            this.dy = 0;
        }

        this.x += this.dx;
        this.y += this.dy;
        updateCollisionBounds();
    }

    /**
     * Renders the ship on the given graphics context. 
     * @param g The Graphics context to draw on.
     */
    public void draw(Graphics g) {
        if (isDestroyed()) {
            return; 
        }
        
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.translate(this.x, this.y);
        g2d.rotate(this.angle);

        double currentSpeed = Math.sqrt(this.dx * this.dx + this.dy * this.dy);
        int imageIndex = 0;

        if (!this.thrusting && currentSpeed < 0.1) { 
            imageIndex = 0;
        } else if (this.thrusting || currentSpeed >= 0.1) { 
            if (currentSpeed < 0.1 && this.thrusting) {
                 imageIndex = 1; 
            } else {
                double speedPerSegment = MAX_SPEED / NUM_BOOST_LEVELS;
                imageIndex = (int) Math.ceil(currentSpeed / speedPerSegment);
                if (imageIndex > NUM_BOOST_LEVELS) {
                    imageIndex = NUM_BOOST_LEVELS;
                }
                if (imageIndex == 0 && currentSpeed >= 0.1) { 
                    imageIndex = 1;
                }
                if (imageIndex == 0 && this.thrusting) {
                    imageIndex = 1;
                }
            }
        }
        
        if (imageIndex >= shipImages.length) { 
            imageIndex = shipImages.length - 1;
        }
        if (imageIndex < 0) {
            imageIndex = 0;
        }

        if (shipImages[imageIndex] != null) {
            g2d.drawImage(shipImages[imageIndex], -width / 2, -height / 2, width, height, null);
        } else {
            g2d.setColor(Color.CYAN); 
            g2d.fillRect(-width / 2, -height / 2, width, height);
        }
        
        drawShieldEffect(g2d);
        
        g2d.dispose();
    }
    
    /**
     * A private helper method to draw the visual effect for the ship's shield.
     * @param g2d The Graphics2D context to draw on.
     */
    private void drawShieldEffect(Graphics2D g2d) {
        ShieldComponent shield = components.getComponent(ShieldComponent.class);
        if (shield == null) return;
        
        float shieldPercent = shield.getShieldPercentage() / 100f;
        
        if (shieldPercent > 0 || shieldHitEffect > 0) {
            if (shieldPercent > 0) {
                shieldVisualStrength = Math.min(1.0f, shieldVisualStrength + 0.05f);
            } else {
                shieldVisualStrength = Math.max(0.0f, shieldVisualStrength - 0.1f);
            }
            
            shieldHitEffect = Math.max(0.0f, shieldHitEffect - 0.05f);
            
            int shieldSize = (int)(Math.max(width, height) * 1.2);
            
            Color baseShieldColor = new Color(0.2f, 0.6f, 1.0f, 
                    0.2f * shieldVisualStrength + 0.6f * shieldHitEffect);
            
            g2d.setComposite(AlphaComposite.SrcOver);
            
            g2d.setColor(baseShieldColor);
            g2d.fillOval(-shieldSize/2, -shieldSize/2, shieldSize, shieldSize);
            
            Color innerShieldColor = new Color(0.6f, 0.8f, 1.0f, 
                    0.15f * shieldVisualStrength + 0.4f * shieldHitEffect);
            g2d.setColor(innerShieldColor);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(-shieldSize/2 + 5, -shieldSize/2 + 5, shieldSize - 10, shieldSize - 10);
        }
    }
}