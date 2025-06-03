package com.tavuc.models.space;

import java.util.List;
import java.util.ArrayList;

public class Space {
    private List<Planet> planets;
    private List<Ship> ships;
    private List<Projectile> projectiles;

    public Space() {
        this.planets = new ArrayList<>();
        this.ships = new ArrayList<>();
        this.projectiles = new ArrayList<>();
    }

    public void addPlanet(Planet planet) {
        this.planets.add(planet);
    }

    public List<Planet> getPlanets() {
        return this.planets;
    }

    public void removePlanet(Planet planet) {
        this.planets.remove(planet);
    }

    public void addShip(Ship ship) {
        this.ships.add(ship);
    }

    public List<Ship> getShips() {
        return this.ships;
    }

    public void removeShip(Ship ship) {
        this.ships.remove(ship);
    }

    public List<Projectile> getProjectiles() {
        return this.projectiles;
    }

    public void addProjectile(Projectile projectile) {
        if (this.projectiles == null) {
            this.projectiles = new ArrayList<>();
        }
        this.projectiles.add(projectile);
    }

    public void removeProjectile(Projectile projectile) {
        if (this.projectiles != null) {
            this.projectiles.remove(projectile);
        }
    }

    public void update(double deltaTime) {
        for (Planet planet : new ArrayList<>(planets)) {
            if (planet.hasUpdate()) {
                planet.update(deltaTime);
            }
        }
        for (Ship ship : new ArrayList<>(ships)) {
            ship.update(deltaTime);
        }
        if (projectiles != null) {
            for (Projectile projectile : new ArrayList<>(projectiles)){
                projectile.update();
            }
        }
    }
}
