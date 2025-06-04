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

public class Ship {

    private BufferedImage[] shipImages;
    private static final int NUM_BOOST_LEVELS = 5;

    private double x, y;
    private double dx, dy;
    private int width, height;
    private double angle;
    private double mass;

    private double rotationInput;
    private boolean thrusting;
    
    private ComponentContainer components;
    private Rectangle collisionBounds;
    private boolean destroyed = false;

    // Ship movement physics constants
    private static final double ROTATION_AMOUNT = Math.toRadians(5.0);
    private static final double THRUST_FORCE = 15.0;
    private static final double MAX_SPEED = 30.0;
    private static final double DAMPING_FACTOR = 0.985;
    private static final double STEERING_ASSIST_FACTOR = 0.08;
    
    // Shield visual effect
    private float shieldVisualStrength = 0.0f;
    private float shieldHitEffect = 0.0f;
    private long lastDamageTime = 0;

    /**
     * Constructor for Ship
     * @param centerX The starting center x position of the ship
     * @param centerY The starting center y position of the ship
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
        
        // Initialize default components
        components.addComponent(new HealthComponent(100f));
        components.addComponent(new ShieldComponent(100f, 10f, 3f)); // 10 shield/sec after 3 sec delay
        
        loadImages();
    }
    
    public ComponentContainer getComponents() {
        return components;
    }
    
    // Convenience methods for health and shield
    public float getHealth() {
        HealthComponent health = components.getComponent(HealthComponent.class);
        return health != null ? health.getHealth() : 0f;
    }
    
    public float getHealthPercentage() {
        HealthComponent health = components.getComponent(HealthComponent.class);
        return health != null ? health.getHealthPercentage() : 0f;
    }
    
    public float getShield() {
        ShieldComponent shield = components.getComponent(ShieldComponent.class);
        return shield != null ? shield.getShield() : 0f;
    }
    
    public float getShieldPercentage() {
        ShieldComponent shield = components.getComponent(ShieldComponent.class);
        return shield != null ? shield.getShieldPercentage() : 0f;
    }
    
    public boolean isDestroyed() {
        return destroyed || getHealth() <= 0;
    }
    
    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    /**
     * Applies damage to the ship, considering shields first.
     * @param amount Amount of damage to apply
     */
    public void takeDamage(double amount) {
        ShieldComponent shield = components.getComponent(ShieldComponent.class);
        HealthComponent health = components.getComponent(HealthComponent.class);

        // Record the damage time for shield recharge delay
        lastDamageTime = System.currentTimeMillis();

        // Visual effect for shield hit
        shieldHitEffect = 1.0f;

        if (shield != null && shield.hasShield()) {
            double shieldDamage = Math.min(amount, shield.getShield());
            amount -= shieldDamage;
            shield.takeDamage((float) shieldDamage);
        }

        if (amount > 0 && health != null) {
            health.takeDamage((float) amount);

            // Check if ship is destroyed
            if (health.getHealth() <= 0) {
                setDestroyed(true);
            }
        }
    }

    /**
     * Updates the ship.
     * @param deltaTime Time passed since the last update in seconds
     */
    public void update(double deltaTime) {
        updatePosition();
    }

    public void setX(double x) {
        this.x = x;
        updateCollisionBounds();
    }

    public void setY(double y) {
        this.y = y;
        updateCollisionBounds();
    }
    
    private void updateCollisionBounds() {
        this.collisionBounds.setLocation((int)x, (int)y);
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public boolean isThrusting() {
        return thrusting;
    }
    
    public Rectangle getCollisionBounds() {
        return collisionBounds;
    }

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
     * @param rotationInput -1 for left, 0 for no rotation, 1 for right.
     */
    public void setRotationInput(double rotationInput) {
        this.rotationInput = rotationInput;
    }

    /**
     * Sets whether the ship is thrusting.
     * @param thrusting true if thrusting, false otherwise.
     */
    public void setThrusting(boolean thrusting) {
        this.thrusting = thrusting;
    }

    /**
     * Gets the x position of the ship's center.
     * @return the x position
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y position of the ship's center.
     * @return the y position
     */
    public double getY() {
        return y;
    }
    
    /**
     * Gets the width of the ship.
     * @return the width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Gets the height of the ship.
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the current angle of the ship.
     * @return the angle in radians
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Gets the dx of the ship.
     * @return dx
     */
    public double getDx() {
        return dx;
    }

    /**
     * Gets the dy of the ship.
     * @return dy
     */
    public double getDy() {
        return dy;
    }

    /**
     * Updates the position and orientation of the ship.
     */
    public void updatePosition() {
        if (isDestroyed()) {
            return; // Don't update destroyed ships
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

        // Apply steering assist when turning to make the ship more maneuverable
        if (this.rotationInput != 0 && (this.dx != 0 || this.dy != 0)) {
            double currentSpeed = Math.sqrt(this.dx * this.dx + this.dy * this.dy);
            if (currentSpeed > 0.01) { 
                double targetDx = Math.sin(this.angle) * currentSpeed;
                double targetDy = -Math.cos(this.angle) * currentSpeed; 

                this.dx = this.dx * (1 - STEERING_ASSIST_FACTOR) + targetDx * STEERING_ASSIST_FACTOR;
                this.dy = this.dy * (1 - STEERING_ASSIST_FACTOR) + targetDy * STEERING_ASSIST_FACTOR;
            }
        }
        
        // Cap maximum speed
        double currentSpeed = Math.sqrt(this.dx * this.dx + this.dy * this.dy);
        if (currentSpeed > MAX_SPEED) {
            this.dx = (this.dx / currentSpeed) * MAX_SPEED;
            this.dy = (this.dy / currentSpeed) * MAX_SPEED;
        }

        // Stop very slow movement when not thrusting
        if (currentSpeed < 0.01 && !this.thrusting) { 
            this.dx = 0;
            this.dy = 0;
        }

        this.x += this.dx;
        this.y += this.dy;
        updateCollisionBounds();
    }

    /**
     * Draws the ship on the given graphics context.
     * The ship is drawn using one of several sprites based on its speed.
     * @param g the Graphics context to draw on
     */
    public void draw(Graphics g) {
        if (isDestroyed()) {
            return; // Don't draw destroyed ships
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
        
        // Draw shields if active
        drawShieldEffect(g2d);
        
        g2d.dispose();
    }
    
    /**
     * Draws the shield effect around the ship.
     */
    private void drawShieldEffect(Graphics2D g2d) {
        ShieldComponent shield = components.getComponent(ShieldComponent.class);
        if (shield == null) return;
        
        float shieldPercent = shield.getShieldPercentage() / 100f;
        
        // Only show shield if it has power or recently hit
        if (shieldPercent > 0 || shieldHitEffect > 0) {
            // Calculate shield visual strength
            if (shieldPercent > 0) {
                // Gradually increase shield visibility when active
                shieldVisualStrength = Math.min(1.0f, shieldVisualStrength + 0.05f);
            } else {
                // Gradually fade shield when depleted
                shieldVisualStrength = Math.max(0.0f, shieldVisualStrength - 0.1f);
            }
            
            // Fade out shield hit effect
            shieldHitEffect = Math.max(0.0f, shieldHitEffect - 0.05f);
            
            // Shield size slightly larger than ship
            int shieldSize = (int)(Math.max(width, height) * 1.2);
            
            // Base shield color (blue with alpha based on shield strength)
            Color baseShieldColor = new Color(0.2f, 0.6f, 1.0f, 
                    0.2f * shieldVisualStrength + 0.6f * shieldHitEffect);
            
            // Draw outer glow
            g2d.setComposite(AlphaComposite.SrcOver);
            
            // Outer shield
            g2d.setColor(baseShieldColor);
            g2d.fillOval(-shieldSize/2, -shieldSize/2, shieldSize, shieldSize);
            
            // Inner shield highlight
            Color innerShieldColor = new Color(0.6f, 0.8f, 1.0f, 
                    0.15f * shieldVisualStrength + 0.4f * shieldHitEffect);
            g2d.setColor(innerShieldColor);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(-shieldSize/2 + 5, -shieldSize/2 + 5, shieldSize - 10, shieldSize - 10);
        }
    }
}
