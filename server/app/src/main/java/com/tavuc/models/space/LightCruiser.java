package com.tavuc.models.space;

import java.util.ArrayList;
import java.util.List;
import com.tavuc.Server; // Added import
import com.tavuc.models.entities.Player; 
import com.tavuc.models.space.Projectile;

public class LightCruiser extends BaseShip {

    private static final float TIME_STEP = 1.0f / 60.0f; 
    private static final float FIRING_ARC_DEGREES = 45.0f; // Wider arc for cruiser
    private static final float EVASION_HEALTH_THRESHOLD = 0.40f; // Evade a bit earlier than attack ships
    private static final float EVASION_DURATION = 4.0f; // Evade for a bit longer

    // Define operational boundaries (can be same as AttackShip or different)
    private static final float OPERATIONAL_AREA_MIN_X = -2500;
    private static final float OPERATIONAL_AREA_MAX_X = 2500;
    private static final float OPERATIONAL_AREA_MIN_Y = -2500;
    private static final float OPERATIONAL_AREA_MAX_Y = 2500;
    private static final float BOUNDARY_AVOIDANCE_MARGIN = 150f;

    private Player targetPlayer;
    private float deploymentRange;
    private float minDeploymentDistance;
    private float attackShipSpawnCooldown;
    private float currentAttackShipSpawnTimer;
    private int maxActiveAttackShips;
    private List<String> currentActiveAttackShipIds;

    private LightCruiserAIState aiState;
    private float evasionTimer = 0f;
    private float firingCooldown; // Will be set based on fireRate
    private float currentFiringTimer;
    private boolean hasWeapon; // Cruiser should generally always have weapons

    public LightCruiser(String entityId, int x, int y, int baseWidth, int baseHeight, Player targetPlayer) {
        super(entityId, x, y, baseWidth, baseHeight,
              2000f, 
              150f,  
              20f,   
              0.8f,  
              1.0f, 
              25.0f); 
        
        this.targetPlayer = targetPlayer;
        this.deploymentRange = 1500f;
        this.minDeploymentDistance = 800f;
        this.attackShipSpawnCooldown = 10.0f; 
        this.currentAttackShipSpawnTimer = this.attackShipSpawnCooldown / 2; 
        this.maxActiveAttackShips = 5;
        this.currentActiveAttackShipIds = new ArrayList<>();
        this.aiState = LightCruiserAIState.INITIALIZING;
        this.firingCooldown = 1.0f / this.fireRate; // Access superclass field directly
        this.currentFiringTimer = 0f; // Can fire immediately if conditions met
        this.hasWeapon = true; 
    }

    @Override
    public void update() {
        updateTimers();
        handleAIState();
        applyPhysics();
    }

    private void updateTimers() {
        if (currentAttackShipSpawnTimer > 0) {
            currentAttackShipSpawnTimer -= TIME_STEP;
        }
        if (currentAttackShipSpawnTimer < 0) {
            currentAttackShipSpawnTimer = 0;
        }

        if (currentFiringTimer > 0) {
            currentFiringTimer -= TIME_STEP;
        }
        if (currentFiringTimer < 0) {
            currentFiringTimer = 0;
        }

        if (evasionTimer > 0) {
            evasionTimer -= TIME_STEP;
        }
        if (evasionTimer < 0) {
            evasionTimer = 0;
        }
    }

    private void handleAIState() {
        // Priority 1: Survival - Evasion
        if (getHealth() / getMaxHealth() < EVASION_HEALTH_THRESHOLD && aiState != LightCruiserAIState.EVADING) {
            aiState = LightCruiserAIState.EVADING;
            evasionTimer = EVASION_DURATION;
            velocityX = 0; // Stop current movement to prepare for evasion
            velocityY = 0;
        }

        // Priority 2: Target validation
        if (targetPlayer == null || targetPlayer.getHealth() <= 0.0f) {
            if (aiState != LightCruiserAIState.EVADING) { // Don't interrupt evasion for target loss
                aiState = LightCruiserAIState.MAINTAINING_POSITION;
            }
            // Slow down if no target and not evading
            if (aiState == LightCruiserAIState.MAINTAINING_POSITION) {
                velocityX *= (1.0f - (getAcceleration() * 0.2f * TIME_STEP)); // Slower dampening
                velocityY *= (1.0f - (getAcceleration() * 0.2f * TIME_STEP));
                if (Math.abs(velocityX) < 0.1f) velocityX = 0;
                if (Math.abs(velocityY) < 0.1f) velocityY = 0;
            }
            // If evading and target is lost, continue evading until timer runs out.
            // The EVADING case will handle transition out of evasion.
            if (aiState != LightCruiserAIState.EVADING) return; // Skip further logic if no valid target and not evading
        }
        
        float playerX = (targetPlayer != null) ? targetPlayer.getX() : getX(); // Use current X if no target for safety
        float playerY = (targetPlayer != null) ? targetPlayer.getY() : getY(); // Use current Y if no target
        float currentX = getX();
        float currentY = getY();
        float deltaX = playerX - currentX;
        float deltaY = playerY - currentY;
        double distanceToTarget = (targetPlayer != null) ? Math.sqrt(deltaX * deltaX + deltaY * deltaY) : Double.MAX_VALUE;

        switch (aiState) {
            case INITIALIZING:
                aiState = LightCruiserAIState.APPROACHING_TARGET;
                break;

            case MAINTAINING_POSITION:
                // Already handled target loss above. If target becomes available again:
                if (targetPlayer != null && targetPlayer.getHealth() > 0.0f) {
                    aiState = LightCruiserAIState.APPROACHING_TARGET;
                } else { // Stay put or drift slowly
                    velocityX *= (1.0f - (getAcceleration() * 0.1f * TIME_STEP));
                    velocityY *= (1.0f - (getAcceleration() * 0.1f * TIME_STEP));
                }
                break;

            case APPROACHING_TARGET:
                if (targetPlayer == null) { aiState = LightCruiserAIState.MAINTAINING_POSITION; break;}
                if (distanceToTarget <= deploymentRange * 0.9f) {
                    aiState = LightCruiserAIState.ENGAGING_TARGET;
                } else {
                    moveToPoint(playerX, playerY, getMaxSpeed());
                }
                break;

            case ENGAGING_TARGET:
                if (targetPlayer == null) { aiState = LightCruiserAIState.MAINTAINING_POSITION; break;}
                // Maintain optimal distance
                if (distanceToTarget < minDeploymentDistance) { // Too close, move away
                    float fleeAngle = (float) Math.atan2(deltaY, deltaX) + (float) Math.PI;
                    float idealX = currentX + (float) Math.cos(fleeAngle) * (minDeploymentDistance * 0.5f); // Move a bit
                    float idealY = currentY + (float) Math.sin(fleeAngle) * (minDeploymentDistance * 0.5f);
                    moveToPoint(idealX, idealY, getMaxSpeed() * 0.6f);
                } else if (distanceToTarget > deploymentRange) { // Too far, move closer
                    aiState = LightCruiserAIState.APPROACHING_TARGET;
                } else { // In ideal band, circle and fire
                    maintainOrbit(playerX, playerY);

                    if (canFireWeapon(playerX, playerY)) {
                        Projectile p = fire();
                        if (p != null && Server.getNetworkManager() != null) {
                            Server.getNetworkManager().addProjectile(p);
                        }
                        currentFiringTimer = firingCooldown;
                    }
                }

                if (canSpawnAttackShip()) { // Check even while engaging
                    aiState = LightCruiserAIState.SPAWNING_ATTACK_SHIPS;
                }
                break;

            case SPAWNING_ATTACK_SHIPS:
                if (targetPlayer == null) { aiState = LightCruiserAIState.MAINTAINING_POSITION; break;}
                spawnAttackShip(); // GameManager handles actual creation and network broadcast
                currentAttackShipSpawnTimer = attackShipSpawnCooldown;
                aiState = LightCruiserAIState.ENGAGING_TARGET; // Return to engagement logic
                break;

            case EVADING:
                if (evasionTimer <= 0) {
                    // Evasion finished, decide next state
                    if (targetPlayer != null && targetPlayer.getHealth() > 0 && getHealth() / getMaxHealth() >= EVASION_HEALTH_THRESHOLD) {
                        aiState = LightCruiserAIState.APPROACHING_TARGET; // Re-engage if healthy enough and target exists
                    } else {
                        aiState = LightCruiserAIState.MAINTAINING_POSITION; // Otherwise, hold position or retreat (if implemented)
                    }
                } else {
                    // Perform evasion maneuver
                    float evadeX, evadeY;
                    if (targetPlayer != null) { // Flee from current target
                        evadeX = getX() - (playerX - getX());
                        evadeY = getY() - (playerY - getY());
                    } else { // Flee towards a 'safe' random direction within operational area
                        double randomAngle = Math.random() * 2 * Math.PI;
                        evadeX = getX() + (float)Math.cos(randomAngle) * 1000; // Large arbitrary distance
                        evadeY = getY() + (float)Math.sin(randomAngle) * 1000;
                    }
                    // Ensure evasion target is within bounds (simple push)
                    evadeX = Math.max(OPERATIONAL_AREA_MIN_X + BOUNDARY_AVOIDANCE_MARGIN, Math.min(OPERATIONAL_AREA_MAX_X - BOUNDARY_AVOIDANCE_MARGIN, evadeX));
                    evadeY = Math.max(OPERATIONAL_AREA_MIN_Y + BOUNDARY_AVOIDANCE_MARGIN, Math.min(OPERATIONAL_AREA_MAX_Y - BOUNDARY_AVOIDANCE_MARGIN, evadeY));
                    moveToPoint(evadeX, evadeY, getMaxSpeed() * 1.1f); // Evade slightly faster
                }
                break;
            
            case RETREATING: // Basic retreat: move to a far point, then maintain position.
                // For now, just transitions to MAINTAINING_POSITION if no specific retreat point logic.
                // Could be expanded to move to a corner of the map or a designated rally point.
                moveToPoint(OPERATIONAL_AREA_MIN_X + BOUNDARY_AVOIDANCE_MARGIN * 2, OPERATIONAL_AREA_MIN_Y + BOUNDARY_AVOIDANCE_MARGIN * 2, getMaxSpeed()*0.5f);
                if (Math.abs(getX() - (OPERATIONAL_AREA_MIN_X + BOUNDARY_AVOIDANCE_MARGIN * 2)) < 100 &&
                    Math.abs(getY() - (OPERATIONAL_AREA_MIN_Y + BOUNDARY_AVOIDANCE_MARGIN * 2)) < 100) {
                    aiState = LightCruiserAIState.MAINTAINING_POSITION;
                }
                break;

            default:
                aiState = LightCruiserAIState.INITIALIZING;
                break;
        }
    }
    
    private void moveToPoint(float targetX, float targetY, float desiredSpeed) {
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

        if (Math.abs(angleDiff) < Math.PI / 2) { 
            float targetVelocityX = (float) Math.cos(getOrientation()) * desiredSpeed;
            float targetVelocityY = (float) Math.sin(getOrientation()) * desiredSpeed;

            float accelX = (targetVelocityX - velocityX) * getAcceleration() * TIME_STEP;
            float accelY = (targetVelocityY - velocityY) * getAcceleration() * TIME_STEP;
            
            velocityX += accelX;
            velocityY += accelY;

            float currentSpeedSq = velocityX * velocityX + velocityY * velocityY;
            if (currentSpeedSq > desiredSpeed * desiredSpeed) {
                float currentSpeed = (float) Math.sqrt(currentSpeedSq);
                velocityX = (velocityX / currentSpeed) * desiredSpeed;
                velocityY = (velocityY / currentSpeed) * desiredSpeed;
            }
        } else {
            velocityX *= (1.0f - (getAcceleration() * 0.2f * TIME_STEP));
            velocityY *= (1.0f - (getAcceleration() * 0.2f * TIME_STEP));
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

    private void maintainOrbit(float playerX, float playerY) {
        float deltaX = playerX - getX();
        float deltaY = playerY - getY();
        float currentDistance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        
        float targetDistance = (deploymentRange + minDeploymentDistance) / 2f;
        float distanceError = currentDistance - targetDistance;
        
        float angleToPlayer = (float) Math.atan2(deltaY, deltaX);
        
        float orbitalSpeed = getMaxSpeed() * 0.3f;
        float radialSpeed = 0f;
        
        if (Math.abs(distanceError) > 50f) {
            radialSpeed = Math.signum(-distanceError) * getMaxSpeed() * 0.2f;
        }
        
        float tangentAngle = angleToPlayer + (float)(Math.PI / 2);
        
        float orbitalVelX = (float)Math.cos(tangentAngle) * orbitalSpeed;
        float orbitalVelY = (float)Math.sin(tangentAngle) * orbitalSpeed;
        
        float radialVelX = (float)Math.cos(angleToPlayer) * radialSpeed;
        float radialVelY = (float)Math.sin(angleToPlayer) * radialSpeed;
        
        float targetVelX = orbitalVelX + radialVelX;
        float targetVelY = orbitalVelY + radialVelY;
        
        float accelFactor = getAcceleration() * TIME_STEP * 0.5f;
        velocityX += (targetVelX - velocityX) * accelFactor;
        velocityY += (targetVelY - velocityY) * accelFactor;
        
        float maxSpeed = getMaxSpeed() * 0.6f;
        float currentSpeed = (float)Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (currentSpeed > maxSpeed) {
            velocityX = (velocityX / currentSpeed) * maxSpeed;
            velocityY = (velocityY / currentSpeed) * maxSpeed;
        }
        
        turnTowards(playerX, playerY);
    }

    private void applyPhysics() {
        float newX = getX() + velocityX * TIME_STEP;
        float newY = getY() + velocityY * TIME_STEP;

        newX = Math.max(OPERATIONAL_AREA_MIN_X - BOUNDARY_AVOIDANCE_MARGIN * 3, Math.min(OPERATIONAL_AREA_MAX_X + BOUNDARY_AVOIDANCE_MARGIN * 3, newX));
        newY = Math.max(OPERATIONAL_AREA_MIN_Y - BOUNDARY_AVOIDANCE_MARGIN * 3, Math.min(OPERATIONAL_AREA_MAX_Y + BOUNDARY_AVOIDANCE_MARGIN * 3, newY));
        
        setPosition((int) newX, (int) newY);
    }

    private boolean canFireWeapon(float playerX, float playerY) {
        if (!hasWeapon || currentFiringTimer > 0 || targetPlayer == null || targetPlayer.getHealth() <= 0) {
            return false;
        }
        if (aiState == LightCruiserAIState.EVADING || aiState == LightCruiserAIState.RETREATING) { // Don't fire when evading/retreating
            return false;
        }

        float deltaX = playerX - getX();
        float deltaY = playerY - getY();
        float angleToTarget = (float) Math.atan2(deltaY, deltaX);
        float angleDifference = Math.abs(angleToTarget - getOrientation());
        
        if (angleDifference > Math.PI) {
            angleDifference = (float) (2 * Math.PI) - angleDifference;
        }
        
        return angleDifference <= Math.toRadians(FIRING_ARC_DEGREES / 2.0f);
    }

    private boolean canSpawnAttackShip() {
        return currentAttackShipSpawnTimer <= 0 && 
               currentActiveAttackShipIds.size() < maxActiveAttackShips &&
               targetPlayer != null && targetPlayer.getHealth() > 0.0f;
    }

    private void spawnAttackShip() {
        if (targetPlayer == null || Server.getGameManager() == null || Server.getNetworkManager() == null) {
            System.out.println("LightCruiser " + getEntityId() + " cannot spawn AttackShip: missing target, GameManager, or NetworkManager.");
            return;
        }

        // Define AttackShip properties (these could be constants or configurable)
        int attackShipWidth = 30; // Example width
        int attackShipHeight = 30; // Example height

        // Spawn slightly offset from the cruiser
        float spawnOffsetX = (float) (Math.cos(getOrientation() + Math.PI / 2) * (getWidth() * 0.6f)); // Spawn to the side
        float spawnOffsetY = (float) (Math.sin(getOrientation() + Math.PI / 2) * (getHeight() * 0.6f));


        AttackShip newAttackShip = Server.getGameManager().spawnAttackShip(
            (int)(getX() + spawnOffsetX), 
            (int)(getY() + spawnOffsetY), 
            attackShipWidth, 
            attackShipHeight, 
            this.targetPlayer, 
            getEntityId()
        );

        if (newAttackShip != null) {
            addAttackShip(newAttackShip.getEntityId());
            Server.getNetworkManager().addAttackShipToBroadcast(newAttackShip); // Ensure NetworkManager handles broadcasting new ship
            System.out.println("LightCruiser " + getEntityId() + " spawned AttackShip: " + newAttackShip.getEntityId());
        } else {
            System.out.println("LightCruiser " + getEntityId() + " failed to spawn AttackShip via GameManager.");
        }
    }


    public float getDeploymentRange() {
        return deploymentRange;
    }

    public void setDeploymentRange(float deploymentRange) {
        this.deploymentRange = deploymentRange;
    }

    public float getMinDeploymentDistance() {
        return minDeploymentDistance;
    }

    public void setMinDeploymentDistance(float minDeploymentDistance) {
        this.minDeploymentDistance = minDeploymentDistance;
    }

    public float getAttackShipSpawnCooldown() {
        return attackShipSpawnCooldown;
    }

    public void setAttackShipSpawnCooldown(float attackShipSpawnCooldown) {
        this.attackShipSpawnCooldown = attackShipSpawnCooldown;
    }

    public float getCurrentAttackShipSpawnTimer() {
        return currentAttackShipSpawnTimer;
    }

    public void setCurrentAttackShipSpawnTimer(float currentAttackShipSpawnTimer) {
        this.currentAttackShipSpawnTimer = currentAttackShipSpawnTimer;
    }

    public int getMaxActiveAttackShips() {
        return maxActiveAttackShips;
    }

    public void setMaxActiveAttackShips(int maxActiveAttackShips) {
        this.maxActiveAttackShips = maxActiveAttackShips;
    }

    public List<String> getCurrentActiveAttackShipIds() {
        return currentActiveAttackShipIds;
    }

    public LightCruiserAIState getAiState() {
        return aiState;
    }

    public void setAiState(LightCruiserAIState aiState) {
        this.aiState = aiState;
    }

    public Player getTargetPlayer() {
        return this.targetPlayer;
    }

    public void setTargetPlayer(Player targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    public void addAttackShip(String shipId) {
        if (currentActiveAttackShipIds.size() < maxActiveAttackShips) {
            currentActiveAttackShipIds.add(shipId);
        }
    }

    public void removeAttackShip(String shipId) {
        currentActiveAttackShipIds.remove(shipId);
    }

    @Override
    public Projectile fire() {
        if (canFire()) {
            this.lastFireTime = System.currentTimeMillis();
            float projectileStartX = getX() + (getWidth() / 2.0f) * (float)Math.cos(getOrientation());
            float projectileStartY = getY() + (getHeight() / 2.0f) * (float)Math.sin(getOrientation());

            float projectileSpeed = 400f; 
            float lifetime = 4.0f; 
            int projectileWidth = 8;
            int projectileHeight = 8;

            Projectile p = new Projectile(
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
            // TODO: Notify GameManager about the new projectile
            // GameManager.getInstance().addProjectile(p);
            System.out.println("LightCruiser " + getEntityId() + " fired a projectile.");
            return p;
        }
        return null;
    }
}
