package com.tavuc.managers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import com.tavuc.controllers.InputBuffer;
import com.tavuc.controllers.InputBuffer.KeyBinding;
import com.tavuc.utils.Vector2D;

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
     * Enum to differentiate between controlling a Player or a Ship.
     */
    public enum ControlTargetType {
        PLAYER,
        SHIP
    }

    private ControlTargetType controlTargetType;
    private Player player;
    private Ship ship;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean shiftPressed;

    // When enabled, player movement is locked to the grid defined by
    // WorldManager.TILE_SIZE and only the four cardinal directions are
    // allowed. This flag can be toggled at runtime for testing.
    private boolean tileMovement = false;

    private final InputBuffer inputBuffer = new InputBuffer(100);

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

    /** Enable or disable tile-based movement mode. */
    public void setTileMovement(boolean enable) {
        this.tileMovement = enable;
    }

    /** Returns whether tile-based movement mode is active. */
    public boolean isTileMovement() {
        return tileMovement;
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
            // REMOVED the space bar handling from here - it's now handled in SpacePanel directly
        } else if (controlTargetType == ControlTargetType.PLAYER) {
            if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
                upPressed = true;
                inputBuffer.registerInput(KeyBinding.MOVE_UP);
            }
            if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
                downPressed = true;
                inputBuffer.registerInput(KeyBinding.MOVE_DOWN);
            }
            if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
                leftPressed = true;
                inputBuffer.registerInput(KeyBinding.MOVE_LEFT);
            }
            if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
                rightPressed = true;
                inputBuffer.registerInput(KeyBinding.MOVE_RIGHT);
            }
            if (keyCode == KeyEvent.VK_SHIFT) {
                shiftPressed = true;
                inputBuffer.registerInput(KeyBinding.SLIDE);
                if (player != null) player.getMovementController().startSlide();
            }
            if (keyCode == KeyEvent.VK_SPACE) {
                inputBuffer.registerInput(KeyBinding.DODGE);
                if (player != null) {
                    player.getMovementController().dodge();
                    player.startDodgeInvulnerability(0.5);
                }
            }
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
            if (keyCode == KeyEvent.VK_SHIFT) shiftPressed = false;
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

        // remove any commands that are outside the buffer window
        inputBuffer.purgeExpired();

        // handle any buffered commands with the highest priority first
        KeyBinding bufferedCommand = inputBuffer.peekHighestPriority();
        if (bufferedCommand != null) {
            switch (bufferedCommand) {
                case SLIDE:
                    player.getMovementController().startSlide();
                    break;
                case DODGE:
                    player.getMovementController().dodge();
                    player.startDodgeInvulnerability(0.5);
                    break;
                default:
                    // movement commands are handled below using key states
                    break;
            }
        }

        double vecX = 0.0;
        double vecY = 0.0;

        if (tileMovement) {
            int vert = (upPressed ? -1 : 0) + (downPressed ? 1 : 0);
            int horz = (rightPressed ? 1 : 0) + (leftPressed ? -1 : 0);
            if (vert != 0 && horz != 0) {
                vecX = 0;
                vecY = 0; // ignore perpendicular presses
            } else if (vert != 0 || horz != 0) {
                vecX = horz;
                vecY = vert;
            }
            player.setMoveVector(vecX, vecY);
            player.setAcceleration(0.0);
            return;
        }

        double angle = player.getDirection();

        if (upPressed && !downPressed) {
            vecX += Math.cos(angle);
            vecY += Math.sin(angle);
        }
        if (downPressed && !upPressed) {
            vecX -= Math.cos(angle);
            vecY -= Math.sin(angle);
        }
        if (leftPressed && !rightPressed) {
            vecX += Math.cos(angle - Math.PI / 2);
            vecY += Math.sin(angle - Math.PI / 2);
        }
        if (rightPressed && !leftPressed) {
            vecX += Math.cos(angle + Math.PI / 2);
            vecY += Math.sin(angle + Math.PI / 2);
        }

        boolean moving = vecX != 0 || vecY != 0;

        if (moving) {
            player.setMoveVector(vecX, vecY);
            player.setAcceleration(1.0);
        } else {
            Vector2D predicted = inputBuffer.getPredictedDirection();
            player.setMoveVector(predicted.getX(), predicted.getY());
            player.setAcceleration(predicted.length() > 0 ? 1.0 : 0.0);
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

    public void simulateKeyPress(int keyCode) {
        KeyEvent keyEvent = new KeyEvent(new java.awt.Component(){}, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);
        this.keyPressed(keyEvent);
    }

    public void simulateKeyRelease(int keyCode) {
        KeyEvent keyEvent = new KeyEvent(new java.awt.Component(){}, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);
        this.keyReleased(keyEvent);
    }
}