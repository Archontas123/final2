package com.tavuc.managers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import com.tavuc.models.entities.Player;
import com.tavuc.models.space.Ship;
import com.tavuc.networking.models.FireRequest;
import com.tavuc.Client; 

public class InputManager implements KeyListener {

    private static InputManager instance;
    private Map<Integer, Boolean> keyStates = new HashMap<>();

    public static synchronized InputManager getInstance() {
        if (instance == null) {
            instance = new InputManager();
        }
        return instance;
    }

    /**
     * Enum to differentiate between controlling a Player, a Ship, or Player in Ship Interior.
     */
    public enum ControlTargetType {
        PLAYER,
        SHIP
    }

    private ControlTargetType controlTargetType;
    private Player player; 
    private Ship ship; 
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    private InputManager() {
        this.controlTargetType = ControlTargetType.PLAYER; 
    }

    public void setControlTarget(ControlTargetType type) {
        this.controlTargetType = type;
        if (type != ControlTargetType.PLAYER) {
            this.player = null;
        }
        if (type != ControlTargetType.SHIP) {
            this.ship = null;
        }
    }

    public void setPlayerTarget(Player player) {
        this.player = player;
    }

    public void setShipTarget(Ship ship) {
        this.ship = ship;
    }


    /**
     * Just here to satisfy the KeyListener interface
     * @param e the KeyEvent
     */
    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * Handles key pressed events setting the appropriate boolean flags or ship states.
     * @param e the KeyEvent
     */
    @Override
    public void keyPressed(KeyEvent e) {
        keyStates.put(e.getKeyCode(), true);
        int keyCode = e.getKeyCode();

        if (controlTargetType == ControlTargetType.SHIP && ship != null) {
            if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
                upPressed = true; 
                ship.setThrusting(true);
            }
            if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
                leftPressed = true;
                ship.setRotationInput(-1.0);
            }
            if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
                rightPressed = true;
                ship.setRotationInput(1.0);
            }
            if (keyCode == KeyEvent.VK_SPACE) {
                FireRequest fireRequest = new FireRequest();
                Client.sendFireRequest(fireRequest); 
            }
        } else if (controlTargetType == ControlTargetType.PLAYER) {
            if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) upPressed = true;
            if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) downPressed = true;
            if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT)  leftPressed = true;
            if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) rightPressed = true;
            updatePlayerMovementInput();
        }
    }

    /**
     * Handles key released events setting the appropriate boolean flags or ship states.
     * @param e the KeyEvent
     */
    @Override
    public void keyReleased(KeyEvent e) {
        keyStates.put(e.getKeyCode(), false);
        int keyCode = e.getKeyCode();

        if (controlTargetType == ControlTargetType.SHIP && ship != null) {
            if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
                upPressed = false;
                ship.setThrusting(false);
            }
            if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
                leftPressed = false;
                if (rightPressed) { 
                    ship.setRotationInput(1.0);
                } else {
                    ship.setRotationInput(0.0);
                }
            }
            if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
                rightPressed = false;
                if (leftPressed) { 
                    ship.setRotationInput(-1.0);
                } else {
                    ship.setRotationInput(0.0);
                }
            }
        } else if (controlTargetType == ControlTargetType.PLAYER && player != null) {
            if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) upPressed = false;
            if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) downPressed = false;
            if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) leftPressed = false;
            if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) rightPressed = false;
            updatePlayerMovementInput();
        }
    }

    public boolean isKeyPressed(int keyCode) {
        return keyStates.getOrDefault(keyCode, false);
    }

    /**
     * Updates the player's movement input based on the current key states.
     * This method is only relevant when controlling a Player.
     */
    private void updatePlayerMovementInput() {
        if (player == null || controlTargetType != ControlTargetType.PLAYER) {
            return; 
        }

        boolean isActuallyMoving = false;
        double targetAngle = player.getDirection(); 

        if (upPressed && !downPressed) { 
            if (leftPressed && !rightPressed) targetAngle = -Math.PI * 3.0 / 4.0; 
            else if (rightPressed && !leftPressed) targetAngle = -Math.PI / 4.0;    
            else targetAngle = -Math.PI / 2.0;                                  
            isActuallyMoving = true;
        } else if (downPressed && !upPressed) { 
            if (leftPressed && !rightPressed) targetAngle = Math.PI * 3.0 / 4.0;  
            else if (rightPressed && !leftPressed) targetAngle = Math.PI / 4.0;     
            else targetAngle = Math.PI / 2.0;                                   
            isActuallyMoving = true;
        } 
        else if (leftPressed && !rightPressed) { 
            targetAngle = Math.PI; 
            isActuallyMoving = true;
        } else if (rightPressed && !leftPressed) { 
            targetAngle = 0;       
            isActuallyMoving = true;
        }

        if (isActuallyMoving) {
            player.setDirection(targetAngle);
            player.setAcceleration(1.0);
        } else {
            player.setAcceleration(0.0); 
        }
    }

    /**
     * Gets the player associated with this InputManager.
     * @return the Player object, or null if not controlling a player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the ship associated with this InputManager.
     * @return the Ship object, or null if not controlling a ship.
     */
    public Ship getShip() {
        return ship;
    }
}
