package com.tavuc.models.space;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream; 
import java.awt.RenderingHints;
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

    private static final double ROTATION_AMOUNT = Math.toRadians(5.0);
    private static final double THRUST_FORCE = 15.0;
    private static final double MAX_SPEED = 30.0;
    private static final double DAMPING_FACTOR = 0.985;
    private static final double STEERING_ASSIST_FACTOR = 0.08; 

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
        
        // Initialize default components
        components.addComponent(new HealthComponent(100f));
        components.addComponent(new ShieldComponent(100f, 5f, 3f)); // 5 shield/sec after 3 sec delay
        
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
    
    public void takeDamage(double amount) {
        ShieldComponent shield = components.getComponent(ShieldComponent.class);
        HealthComponent health = components.getComponent(HealthComponent.class);
        
        if (shield != null && shield.hasShield()) {
            double shieldDamage = Math.min(amount, shield.getShield());
            shield.takeDamage((float)shieldDamage);
            amount -= shieldDamage;
        }
        
        if (amount > 0 && health != null) {
            health.takeDamage((float)amount);
        }
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
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
    }

    /**
     * Draws the ship on the given graphics context.
     * The ship is drawn using one of several sprites based on its speed.
     * @param g the Graphics context to draw on
     */
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

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
        g2d.dispose();
    }

    /**
     * Updates the ship's state.
     * @param deltaTime The time elapsed since the last update.
     */
    public void update(double deltaTime) {
        updatePosition();
        
        // Update shield recharge
        ShieldComponent shield = components.getComponent(ShieldComponent.class);
        if (shield != null) {
            shield.update((float)deltaTime);
        }
    }
}
