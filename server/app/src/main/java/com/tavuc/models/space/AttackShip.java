package com.tavuc.models.space;

import com.tavuc.Server; 
import com.tavuc.models.entities.Player;
import com.tavuc.models.space.Projectile;

public class AttackShip extends BaseShip {

    private static final float TIME_STEP = 1.0f / 60.0f;
    private static final float EVASION_HEALTH_THRESHOLD = 0.30f; 

    private static final float OPERATIONAL_AREA_MIN_X = -2000;
    private static final float OPERATIONAL_AREA_MAX_X = 2000;
    private static final float OPERATIONAL_AREA_MIN_Y = -2000;
    private static final float OPERATIONAL_AREA_MAX_Y = 2000;
    private static final float BOUNDARY_AVOIDANCE_MARGIN = 100f;

    private static final float MIN_FIRING_RANGE = 50f;
    private static final float MAX_FIRING_RANGE = 300f;
    private static final float DIVE_RANGE = 200f;
    private static final float RETREAT_DISTANCE = 600f;
    private static final float BURST_FIRE_COOLDOWN = 0.2f;
    private static final int SHOTS_PER_BURST = 4;
    private static final float COOLDOWN_DURATION = 2.5f;
    private static final float EVASION_DURATION = 3.0f;

    private AttackShipAIState aiState;
    private Player targetPlayerObject; 
    private String parentCruiserId;
    
    private float burstFireTimer;
    private int shotsInCurrentBurst;
    private float cooldownTimer;
    private float evasionTimer;
    private boolean hasWeapon;

    public AttackShip(String entityId, int x, int y, int width, int height, Player targetPlayer, String parentCruiserId) {
        super(entityId, x, y, width, height,
              100f, 
              350f,  
              150f, 
              2.5f,  
              5.0f,  
              10.0f); 
        
        this.aiState = AttackShipAIState.INITIALIZING;
        this.targetPlayerObject = targetPlayer;
        this.parentCruiserId = parentCruiserId;
        this.hasWeapon = true;
        this.burstFireTimer = 0f;
        this.shotsInCurrentBurst = 0;
        this.cooldownTimer = 0f;
        this.evasionTimer = 0f;
    }

    @Override
    public void update() {
        updateTimers();
        validateTarget(); 
        handleAIState();
        applyPhysics();
    }

    private void updateTimers() {
        if (burstFireTimer > 0) {
            burstFireTimer -= TIME_STEP;
        }
        if (cooldownTimer > 0) {
            cooldownTimer -= TIME_STEP;
        }
        if (evasionTimer > 0) {
            evasionTimer -= TIME_STEP;
        }
    }

    private void validateTarget() {
        if (this.targetPlayerObject != null && targetPlayerObject.getHealth() <= 0) {
            this.targetPlayerObject = null; 
        }
    }

    private void handleAIState() {
        if (getHealth() / getMaxHealth() < EVASION_HEALTH_THRESHOLD && 
            aiState != AttackShipAIState.EVADING && 
            aiState != AttackShipAIState.RETURNING_TO_CRUISER) {
            aiState = AttackShipAIState.EVADING;
            evasionTimer = EVASION_DURATION; 
            velocityX = 0; 
            velocityY = 0;
        }
        
        if (targetPlayerObject == null) {
            System.out.println("[AttackShip " + getEntityId() + "] No target player - returning to cruiser");
            if (aiState != AttackShipAIState.RETURNING_TO_CRUISER && aiState != AttackShipAIState.EVADING) {
                aiState = AttackShipAIState.RETURNING_TO_CRUISER;
            }
            if (aiState != AttackShipAIState.RETURNING_TO_CRUISER && aiState != AttackShipAIState.EVADING) {
                velocityX *= (1.0f - (getAcceleration() * 0.5f * TIME_STEP));
                velocityY *= (1.0f - (getAcceleration() * 0.5f * TIME_STEP));
                if (Math.abs(velocityX) < 0.1f) velocityX = 0;
                if (Math.abs(velocityY) < 0.1f) velocityY = 0;
            }
            if (aiState != AttackShipAIState.EVADING && aiState != AttackShipAIState.RETURNING_TO_CRUISER) {
                return; 
            }
        }

        float playerX = (targetPlayerObject != null) ? targetPlayerObject.getX() : 0; 
        float playerY = (targetPlayerObject != null) ? targetPlayerObject.getY() : 0; 
        float currentX = getX();
        float currentY = getY();
        
        System.out.println("[AttackShip " + getEntityId() + "] State: " + aiState + ", Target: (" + playerX + "," + playerY + "), Current: (" + currentX + "," + currentY + ")");
        
        double distanceToPlayer = (targetPlayerObject != null) ? 
            Math.sqrt(Math.pow(playerX - currentX, 2) + Math.pow(playerY - currentY, 2)) : Double.MAX_VALUE;

        switch (aiState) {
            case INITIALIZING:
                aiState = AttackShipAIState.APPROACHING;
                break;

            case APPROACHING:
                if (targetPlayerObject == null) { 
                    aiState = AttackShipAIState.RETURNING_TO_CRUISER; 
                    break;
                }
                
                moveDirectlyToward(playerX, playerY, getMaxSpeed());
                
                if (distanceToPlayer <= DIVE_RANGE) {
                    aiState = AttackShipAIState.DIVING;
                    shotsInCurrentBurst = 0;
                    burstFireTimer = 0f;
                }
                break;

            case DIVING:
                if (targetPlayerObject == null) { 
                    aiState = AttackShipAIState.RETREATING; 
                    break;
                }
                
                turnTowards(playerX, playerY);
                
                float diveSpeed = getMaxSpeed() * 0.3f;
                moveDirectlyToward(playerX, playerY, diveSpeed);
                
                if (canFireInBurst(playerX, playerY) && burstFireTimer <= 0) {
                    Projectile projectile = fire();
                    if (projectile != null) {
                        if (Server.getNetworkManager() != null) {
                            Server.getNetworkManager().addProjectile(projectile);
                        }
                        shotsInCurrentBurst++;
                        burstFireTimer = BURST_FIRE_COOLDOWN;
                    }
                }
                
                if (shotsInCurrentBurst >= SHOTS_PER_BURST || distanceToPlayer < MIN_FIRING_RANGE) {
                    aiState = AttackShipAIState.RETREATING;
                }
                break;

            case RETREATING:
                float retreatX, retreatY;
                if (targetPlayerObject != null) {
                    float deltaX = currentX - playerX;
                    float deltaY = currentY - playerY;
                    float distance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    if (distance > 0) {
                        retreatX = currentX + (deltaX / distance) * RETREAT_DISTANCE;
                        retreatY = currentY + (deltaY / distance) * RETREAT_DISTANCE;
                    } else {
                        retreatX = currentX + RETREAT_DISTANCE;
                        retreatY = currentY;
                    }
                } else {
                    retreatX = currentX + RETREAT_DISTANCE;
                    retreatY = currentY;
                }
                
                retreatX = Math.max(OPERATIONAL_AREA_MIN_X + BOUNDARY_AVOIDANCE_MARGIN, 
                          Math.min(OPERATIONAL_AREA_MAX_X - BOUNDARY_AVOIDANCE_MARGIN, retreatX));
                retreatY = Math.max(OPERATIONAL_AREA_MIN_Y + BOUNDARY_AVOIDANCE_MARGIN, 
                          Math.min(OPERATIONAL_AREA_MAX_Y - BOUNDARY_AVOIDANCE_MARGIN, retreatY));
                
                moveDirectlyToward(retreatX, retreatY, getMaxSpeed());
                
                if (targetPlayerObject != null && distanceToPlayer >= RETREAT_DISTANCE * 0.8f) {
                    aiState = AttackShipAIState.COOLDOWN;
                    cooldownTimer = COOLDOWN_DURATION;
                }
                break;

            case COOLDOWN:
                velocityX *= (1.0f - (getAcceleration() * 0.3f * TIME_STEP));
                velocityY *= (1.0f - (getAcceleration() * 0.3f * TIME_STEP));
                
                if (cooldownTimer <= 0) {
                    if (targetPlayerObject != null && targetPlayerObject.getHealth() > 0) {
                        aiState = AttackShipAIState.APPROACHING;
                    } else {
                        aiState = AttackShipAIState.RETURNING_TO_CRUISER;
                    }
                }
                break;

            case EVADING:
                if (evasionTimer <= 0) {
                    if (targetPlayerObject != null && targetPlayerObject.getHealth() > 0 && 
                        getHealth() / getMaxHealth() >= EVASION_HEALTH_THRESHOLD) {
                        aiState = AttackShipAIState.APPROACHING;
                    } else {
                        aiState = AttackShipAIState.RETURNING_TO_CRUISER;
                    }
                } else {
                    float evadeX, evadeY;
                    if (targetPlayerObject != null) {
                        evadeX = getX() - (targetPlayerObject.getX() - getX()); 
                        evadeY = getY() - (targetPlayerObject.getY() - getY());
                    } else {
                        float centerX = (OPERATIONAL_AREA_MIN_X + OPERATIONAL_AREA_MAX_X) / 2;
                        float centerY = (OPERATIONAL_AREA_MIN_Y + OPERATIONAL_AREA_MAX_Y) / 2;
                        if (Math.abs(getX() - centerX) > (OPERATIONAL_AREA_MAX_X - OPERATIONAL_AREA_MIN_X) / 4 || 
                            Math.abs(getY() - centerY) > (OPERATIONAL_AREA_MAX_Y - OPERATIONAL_AREA_MIN_Y) / 4) {
                            evadeX = getX() + (getX() - centerX);
                            evadeY = getY() + (getY() - centerY);
                        } else {
                            double randomAngle = Math.random() * 2 * Math.PI;
                            evadeX = getX() + (float)Math.cos(randomAngle) * 500;
                            evadeY = getY() + (float)Math.sin(randomAngle) * 500;
                        }
                    }
                    evadeX = Math.max(OPERATIONAL_AREA_MIN_X - BOUNDARY_AVOIDANCE_MARGIN*2, 
                            Math.min(OPERATIONAL_AREA_MAX_X + BOUNDARY_AVOIDANCE_MARGIN*2, evadeX));
                    evadeY = Math.max(OPERATIONAL_AREA_MIN_Y - BOUNDARY_AVOIDANCE_MARGIN*2, 
                            Math.min(OPERATIONAL_AREA_MAX_Y + BOUNDARY_AVOIDANCE_MARGIN*2, evadeY));
                    moveDirectlyToward(evadeX, evadeY, getMaxSpeed() * 1.1f);
                }
                break;

            case RETURNING_TO_CRUISER:
                LightCruiser parent = null;
                if (Server.getGameManager() != null) {
                    parent = Server.getGameManager().findLightCruiserById(parentCruiserId);
                }

                if (parent != null) {
                    float cruiserX = parent.getX();
                    float cruiserY = parent.getY();
                    moveDirectlyToward(cruiserX, cruiserY, getMaxSpeed() * 0.7f);
                    double distanceToCruiser = Math.sqrt(Math.pow(cruiserX - getX(), 2) + Math.pow(cruiserY - getY(), 2));
                    if (distanceToCruiser < 100f) {
                        velocityX *= 0.8f; 
                        velocityY *= 0.8f;
                        if (targetPlayerObject != null && targetPlayerObject.getHealth() > 0 && 
                            getHealth() / getMaxHealth() >= EVASION_HEALTH_THRESHOLD) {
                            aiState = AttackShipAIState.APPROACHING;
                        }
                    }
                } else {
                    if (targetPlayerObject != null && targetPlayerObject.getHealth() > 0 && 
                        getHealth() / getMaxHealth() >= EVASION_HEALTH_THRESHOLD) {
                        aiState = AttackShipAIState.APPROACHING;
                    } else {
                        velocityX *= (1.0f - (getAcceleration() * 0.7f * TIME_STEP));
                        velocityY *= (1.0f - (getAcceleration() * 0.7f * TIME_STEP));
                    }
                }
                break;

            default:
                aiState = AttackShipAIState.INITIALIZING;
                break;
        }
    }

    private void moveDirectlyToward(float targetX, float targetY, float desiredSpeed) {
        float deltaX = targetX - getX();
        float deltaY = targetY - getY();
        float distance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        
        if (distance > 0.1f) {
            float targetAngle = (float) Math.atan2(deltaY, deltaX);
            
            float angleDiff = targetAngle - getOrientation();
            while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
            while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

            float turnAmount = getTurnRate() * TIME_STEP;
            if (Math.abs(angleDiff) < turnAmount) {
                setOrientation(targetAngle);
            } else {
                setOrientation(getOrientation() + Math.signum(angleDiff) * turnAmount);
            }

            if (Math.abs(angleDiff) < Math.PI / 3) {
                float targetVelocityX = (deltaX / distance) * desiredSpeed;
                float targetVelocityY = (deltaY / distance) * desiredSpeed;

                float accelComponent = getAcceleration() * TIME_STEP; 

                velocityX += Math.max(-accelComponent, Math.min(accelComponent, targetVelocityX - velocityX));
                velocityY += Math.max(-accelComponent, Math.min(accelComponent, targetVelocityY - velocityY));
                
                float currentSpeedSq = velocityX * velocityX + velocityY * velocityY;
                if (currentSpeedSq > desiredSpeed * desiredSpeed) {
                    float currentSpeedMagnitude = (float) Math.sqrt(currentSpeedSq);
                    velocityX = (velocityX / currentSpeedMagnitude) * desiredSpeed;
                    velocityY = (velocityY / currentSpeedMagnitude) * desiredSpeed;
                }
            } else {
                velocityX *= (1.0f - (getAcceleration() * 0.2f * TIME_STEP)); 
                velocityY *= (1.0f - (getAcceleration() * 0.2f * TIME_STEP));
            }
        }
    }

    private void turnTowards(float targetX, float targetY) {
        float deltaX = targetX - getX();
        float deltaY = targetY - getY();
        float targetAngle = (float) Math.atan2(deltaY, deltaX);

        float angleDiff = targetAngle - getOrientation();
        while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
        while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

        float turnAmount = getTurnRate() * TIME_STEP;
        if (Math.abs(angleDiff) < turnAmount) {
            setOrientation(targetAngle);
        } else {
            setOrientation(getOrientation() + Math.signum(angleDiff) * turnAmount);
        }
    }
    
    private boolean canFireInBurst(float playerX, float playerY) {
        if (!hasWeapon || targetPlayerObject == null || targetPlayerObject.getHealth() <= 0) {
            return false;
        }
        
        if (aiState == AttackShipAIState.EVADING || aiState == AttackShipAIState.RETURNING_TO_CRUISER) {
            return false;
        }

        float deltaX = playerX - getX();
        float deltaY = playerY - getY();
        float distanceToTarget = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        
        if (distanceToTarget < MIN_FIRING_RANGE || distanceToTarget > MAX_FIRING_RANGE) {
            return false;
        }

        float angleToTarget = (float) Math.atan2(deltaY, deltaX);
        float angleDifference = Math.abs(angleToTarget - getOrientation());
        
        if (angleDifference > Math.PI) {
            angleDifference = (float) (2 * Math.PI) - angleDifference;
        }
        
        return angleDifference <= Math.toRadians(15.0f);
    }

    @Override
    public Projectile fire() {
        if (canFire()) {
            this.lastFireTime = System.currentTimeMillis();
            float projectileStartX = getX() + (getWidth() / 2.0f) * (float)Math.cos(getOrientation());
            float projectileStartY = getY() + (getHeight() / 2.0f) * (float)Math.sin(getOrientation());
            float projectileSpeed = 500f; 
            float lifetime = 3.0f; 
            int projectileWidth = 5;
            int projectileHeight = 5;

            return new Projectile(
                java.util.UUID.randomUUID().toString(), 
                (int)projectileStartX, 
                (int)projectileStartY, 
                projectileWidth,
                projectileHeight,
                getOrientation(), 
                projectileSpeed, 
                getProjectileDamage(),
                getEntityId(), 
                lifetime 
            );
        }
        return null;
    }

    private void applyPhysics() {
        float newX = getX() + velocityX * TIME_STEP;
        float newY = getY() + velocityY * TIME_STEP;
        
        newX = Math.max(OPERATIONAL_AREA_MIN_X - BOUNDARY_AVOIDANCE_MARGIN * 5, 
              Math.min(OPERATIONAL_AREA_MAX_X + BOUNDARY_AVOIDANCE_MARGIN * 5, newX));
        newY = Math.max(OPERATIONAL_AREA_MIN_Y - BOUNDARY_AVOIDANCE_MARGIN * 5, 
              Math.min(OPERATIONAL_AREA_MAX_Y + BOUNDARY_AVOIDANCE_MARGIN * 5, newY));

        setPosition((int) newX, (int) newY);
    }

    public String getParentCruiserId() {
        return parentCruiserId;
    }

    public AttackShipAIState getAiState() {
        return aiState;
    }

    public void setAiState(AttackShipAIState aiState) {
        this.aiState = aiState;
    }

    public Player getTargetPlayerObject() {
        return targetPlayerObject;
    }

    public void setTargetPlayerObject(Player targetPlayerObject) {
        this.targetPlayerObject = targetPlayerObject;
    }

    public boolean isHasWeapon() {
        return hasWeapon;
    }

    public void setHasWeapon(boolean hasWeapon) {
        this.hasWeapon = hasWeapon;
    }
}
