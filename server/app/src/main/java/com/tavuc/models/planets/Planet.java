package com.tavuc.models.planets;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import com.tavuc.utils.PerlinUtility;

import java.awt.Color;
import java.awt.Point;

import java.util.List;  
import java.util.ArrayList;  

public class Planet {

    private int planetId;
    private String name;
    private int width;
    private int height;
    private double gravity;
    private double tempeature;
    private PlanetType type;
    private long seed;
    private ColorPallete colorPallete;
    private Map<Point, Chunk> chunks;
    private PerlinUtility perlinUtility;
    private List<Moon> moons;
    private int galaxyX;
    private int galaxyY;

    /**
     * Constructor for Planet
     * @param planetId the unique ID of the planet
     * @param name the name of the planet
     * @param type the type of the planet
     * @param width the width of the planet
     * @param height the height of the planet
     * @param gravity the gravity of the planet
     * @param tempeature the temperature of the planet
     * @param seed the seed for random generation
     * @param galaxyX the x-coordinate in the galaxy
     * @param galaxyY the y-coordinate in the galaxy
     */
    public Planet(int planetId, String name, PlanetType type, int width, int height, double gravity, double tempeature, long seed, int galaxyX, int galaxyY) {
        this.planetId = planetId;
        this.name = name;
        this.width = width * 2;
        this.height = height * 2; 
        this.gravity = gravity;
        this.tempeature = tempeature;
        this.type = type;
        this.seed = seed;
        this.galaxyX = galaxyX;
        this.galaxyY = galaxyY;
        this.colorPallete = generateColorPalette();
        this.chunks = new HashMap<>();
        this.perlinUtility = new PerlinUtility(seed);
        this.moons = new ArrayList<>();
    }

    /**
     * Get the chunk at the specified point
     * @param chunkX the leftmost x coordinate of the chunk
     * @param chunkY the bottommost y coordinate of the chunk
     * @return the chunk at the specified point
     */
    public Chunk getChunk(int chunkX, int chunkY) {
        Point chunkPos = new Point(chunkX, chunkY);
        if (chunks.containsKey(chunkPos)) {
            return chunks.get(chunkPos);
        }
        Chunk newChunk = generateChunk(chunkX, chunkY);
        chunks.put(chunkPos, newChunk);
        return newChunk;
    }

    /**
     * Generates a chunk at the specified coordinates
     * @param chunkX the leftmost x coordinate of the chunk
     * @param chunkY the bottommost y coordinate of the chunk
     * @return the generated chunk
     */
    private Chunk generateChunk(int chunkX, int chunkY){
        Chunk chunk = new Chunk(chunkX, chunkY, this);
        Tile[][] tiles = chunk.getTiles(); 
        double scale = 0.05; 
        int octaves = 4;
        double persistence = 0.5;

        for (int localX = 0; localX < 16; localX++) {
            for (int localY = 0; localY < 16; localY++) {
                int globalX = chunkX * 16 + localX;
                int globalY = chunkY * 16 + localY;

                double noiseValue = perlinUtility.octaveNoise(globalX * scale, globalY * scale, octaves, persistence);
                
                ColorType colorType;
                String tileTypeString;

                if (noiseValue < -0.2) {
                    colorType = ColorType.PRIMARY_LIQUID;
                    tileTypeString = "water";
                } else if (noiseValue < 0.1) {
                    colorType = ColorType.PRIMARY_SURFACE;
                    tileTypeString = "sand";
                } else if (noiseValue < 0.4) {
                    colorType = ColorType.SECONDARY_SURFACE;
                    tileTypeString = "grass";
                } else if (noiseValue < 0.7) {
                    colorType = ColorType.TERTIARY_SURFACE;
                    tileTypeString = "dirt";
                } else {
                    colorType = ColorType.ROCK;
                    tileTypeString = "rock";
                }
                
                tiles[localX][localY] = new Tile(globalX, globalY, this, tileTypeString, colorType);
            }
        }
        return chunk;
    }

    /**
     * Generates a color palette based on the planet type and seed
     * @return the generated color palette
     */
    public ColorPallete generateColorPalette() {
        Random random = new Random(seed);
        Color primarySurface = Color.BLACK;
        Color primaryLiquid = Color.BLACK;
        Color secondarySurface = Color.BLACK;
        Color tertiarySurface = Color.BLACK;
        Color hueShift = Color.BLACK;
        Color rock = Color.BLACK;

        Function<Float, Float> clamp = val -> Math.max(0.0f, Math.min(1.0f, val));

        switch (type) {
            case Terrestrial:
                primarySurface = Color.getHSBColor(
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f), 
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.2f)  
                );
                primaryLiquid = Color.getHSBColor(
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.2f), 
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f)  
                );
                secondarySurface = Color.getHSBColor(
                    clamp.apply(0.25f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                tertiarySurface = Color.getHSBColor(
                    clamp.apply(0.35f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                hueShift = Color.getHSBColor(
                    clamp.apply(primarySurface.getRGB() != Color.BLACK.getRGB() ? Color.RGBtoHSB(primarySurface.getRed(), primarySurface.getGreen(), primarySurface.getBlue(), null)[0] + (random.nextFloat() - 0.5f) * 0.05f : random.nextFloat()),
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                rock = Color.getHSBColor(
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                break;
            case Ice:
                primarySurface = Color.getHSBColor(
                    clamp.apply(0.55f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.2f), 
                    clamp.apply(0.9f + (random.nextFloat() - 0.5f) * 0.1f)  
                );
                primaryLiquid = Color.getHSBColor(
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.05f),
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                secondarySurface = Color.getHSBColor(
                    clamp.apply(0.58f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.2f + (random.nextFloat() - 0.5f) * 0.15f),
                    clamp.apply(0.8f + (random.nextFloat() - 0.5f) * 0.15f)
                );
                tertiarySurface = Color.getHSBColor(
                    clamp.apply(0.62f + (random.nextFloat() - 0.5f) * 0.05f),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                hueShift = Color.getHSBColor(
                    clamp.apply(primarySurface.getRGB() != Color.BLACK.getRGB() ? Color.RGBtoHSB(primarySurface.getRed(), primarySurface.getGreen(), primarySurface.getBlue(), null)[0] + (random.nextFloat() - 0.5f) * 0.05f : random.nextFloat()),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.85f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                rock = Color.getHSBColor(
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                break;
            case Volcanic:
                primarySurface = Color.getHSBColor(
                    clamp.apply(0.05f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.9f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f)  
                );
                primaryLiquid = Color.getHSBColor(
                    clamp.apply(0.08f + (random.nextFloat() - 0.5f) * 0.05f), 
                    clamp.apply(1.0f),                                    
                    clamp.apply(0.9f + (random.nextFloat() - 0.5f) * 0.1f)  
                );
                secondarySurface = Color.getHSBColor(
                    clamp.apply(0.02f + (random.nextFloat() - 0.5f) * 0.05f),
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                tertiarySurface = Color.getHSBColor(
                    random.nextFloat() * 0.1f, 
                    clamp.apply(0.05f + random.nextFloat() * 0.1f), 
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                hueShift = Color.getHSBColor(
                    clamp.apply(primarySurface.getRGB() != Color.BLACK.getRGB() ? Color.RGBtoHSB(primarySurface.getRed(), primarySurface.getGreen(), primarySurface.getBlue(), null)[0] + (random.nextFloat() - 0.5f) * 0.03f : random.nextFloat()),
                    clamp.apply(0.95f + (random.nextFloat() - 0.5f) * 0.05f),
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                rock = Color.getHSBColor(
                    random.nextFloat() * 0.1f,
                    clamp.apply(random.nextFloat() * 0.1f),
                    clamp.apply(0.2f + (random.nextFloat() - 0.5f) * 0.15f)
                );
                break;
            case Ecumenopoleis: 
                primarySurface = Color.getHSBColor( 
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f) 
                );
                primaryLiquid = Color.getHSBColor( 
                    clamp.apply(0.55f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.2f), 
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                secondarySurface = Color.getHSBColor(
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f), 
                    clamp.apply(0.05f + (random.nextFloat() - 0.5f) * 0.05f), 
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                tertiarySurface = Color.getHSBColor( 
                    clamp.apply(random.nextBoolean() ? (0.15f + random.nextFloat() * 0.05f) : (0.5f + random.nextFloat() * 0.05f)), 
                    clamp.apply(0.8f + random.nextFloat() * 0.2f), 
                    clamp.apply(0.9f + random.nextFloat() * 0.1f)
                );
                hueShift = Color.getHSBColor(
                    clamp.apply(0.65f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.2f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                rock = Color.getHSBColor( 
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.05f + (random.nextFloat() - 0.5f) * 0.05f), 
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                break;
            case Desert:
                primarySurface = Color.getHSBColor(
                    clamp.apply(0.08f + (random.nextFloat() - 0.5f) * 0.08f), 
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.2f),  
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.2f)   
                );
                primaryLiquid = Color.getHSBColor(
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.05f),
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                secondarySurface = Color.getHSBColor(
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.05f),
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.85f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                tertiarySurface = Color.getHSBColor(
                    clamp.apply(0.07f + (random.nextFloat() - 0.5f) * 0.05f),
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                hueShift = Color.getHSBColor(
                    clamp.apply(primarySurface.getRGB() != Color.BLACK.getRGB() ? Color.RGBtoHSB(primarySurface.getRed(), primarySurface.getGreen(), primarySurface.getBlue(), null)[0] + (random.nextFloat() - 0.5f) * 0.03f : random.nextFloat()),
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.75f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                rock = Color.getHSBColor(
                    clamp.apply(0.06f + (random.nextFloat() - 0.5f) * 0.04f),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                break;
            case Forest:
                primarySurface = Color.getHSBColor(
                    clamp.apply(0.35f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.2f),  
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f)   
                );
                primaryLiquid = Color.getHSBColor(
                    clamp.apply(random.nextBoolean() ? (0.45f + (random.nextFloat() - 0.5f) * 0.1f) : (0.6f + (random.nextFloat() - 0.5f) * 0.1f)),
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                secondarySurface = Color.getHSBColor(
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.65f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                tertiarySurface = Color.getHSBColor(
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.05f), 
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                hueShift = Color.getHSBColor(
                    clamp.apply(primarySurface.getRGB() != Color.BLACK.getRGB() ? Color.RGBtoHSB(primarySurface.getRed(), primarySurface.getGreen(), primarySurface.getBlue(), null)[0] - 0.05f + random.nextFloat() * 0.1f : random.nextFloat()),
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.65f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                rock = Color.getHSBColor(
                    clamp.apply(0.15f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.2f + (random.nextFloat() - 0.5f) * 0.15f),
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                break;
            case Oceanic:
                primarySurface = Color.getHSBColor(
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.8f + (random.nextFloat() - 0.5f) * 0.2f),  
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.2f)   
                );
                primaryLiquid = Color.getHSBColor(
                    clamp.apply(0.62f + (random.nextFloat() - 0.5f) * 0.08f),
                    clamp.apply(0.75f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.55f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                secondarySurface = Color.getHSBColor(
                    clamp.apply(0.55f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.8f + (random.nextFloat() - 0.5f) * 0.15f)
                );
                tertiarySurface = Color.getHSBColor(
                    clamp.apply(random.nextBoolean() ? (0.25f + (random.nextFloat() - 0.5f) * 0.1f) : (0.65f + (random.nextFloat() - 0.5f) * 0.05f)),
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                hueShift = Color.getHSBColor(
                    clamp.apply(primarySurface.getRGB() != Color.BLACK.getRGB() ? Color.RGBtoHSB(primarySurface.getRed(), primarySurface.getGreen(), primarySurface.getBlue(), null)[0] - 0.05f + random.nextFloat() * 0.1f : random.nextFloat()),
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.75f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                rock = Color.getHSBColor(
                    clamp.apply(random.nextBoolean() ? (0.6f + (random.nextFloat() - 0.5f) * 0.1f) : (0.05f + random.nextFloat() * 0.1f)), 
                    clamp.apply(random.nextBoolean() ? (0.1f + (random.nextFloat() - 0.5f) * 0.1f) : (0.7f + random.nextFloat() * 0.2f)),
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                break;
            case Rocky:
                primarySurface = Color.getHSBColor(
                    clamp.apply(0.08f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.2f + (random.nextFloat() - 0.5f) * 0.15f), 
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f)   
                );
                primaryLiquid = Color.getHSBColor(
                    clamp.apply(0.55f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.2f),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                secondarySurface = Color.getHSBColor(
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.15f),
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                tertiarySurface = Color.getHSBColor(
                    clamp.apply(random.nextBoolean() ? (0.05f + (random.nextFloat() - 0.5f) * 0.05f) : (0.6f + (random.nextFloat() - 0.5f) * 0.05f)),
                    clamp.apply(0.15f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                hueShift = Color.getHSBColor(
                    clamp.apply(primarySurface.getRGB() != Color.BLACK.getRGB() ? Color.RGBtoHSB(primarySurface.getRed(), primarySurface.getGreen(), primarySurface.getBlue(), null)[0] + (random.nextFloat() - 0.5f) * 0.02f : random.nextFloat()),
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.05f),
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                rock = Color.getHSBColor(
                    clamp.apply(0.07f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.15f + (random.nextFloat() - 0.5f) * 0.1f),
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                break;
            case Cave: 
                primarySurface = Color.getHSBColor( 
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.15f), 
                    clamp.apply(0.15f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.25f + (random.nextFloat() - 0.5f) * 0.15f) 
                );
                primaryLiquid = Color.getHSBColor( 
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.2f), 
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f), 
                    clamp.apply(0.2f + (random.nextFloat() - 0.5f) * 0.1f) 
                );
                secondarySurface = Color.getHSBColor(
                    clamp.apply(0.15f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                tertiarySurface = Color.getHSBColor( 
                    clamp.apply( (float) (0.5f + random.nextDouble() * 0.3f) ), 
                    clamp.apply(0.7f + random.nextFloat() * 0.2f), 
                    clamp.apply(0.6f + random.nextFloat() * 0.3f)  
                );
                hueShift = Color.getHSBColor( 
                    clamp.apply(tertiarySurface.getRGB() != Color.BLACK.getRGB() ? Color.RGBtoHSB(tertiarySurface.getRed(), tertiarySurface.getGreen(), tertiarySurface.getBlue(), null)[0] : random.nextFloat()),
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.3f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                rock = Color.getHSBColor( 
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.2f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                break;
            case Gas: 
                
                float baseHueGas = random.nextFloat(); 
                primarySurface = Color.getHSBColor( 
                    clamp.apply(baseHueGas + (random.nextFloat() - 0.5f) * 0.2f), 
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.3f), 
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                primaryLiquid = primarySurface; 
                secondarySurface = Color.getHSBColor( 
                    clamp.apply(baseHueGas + 0.3f + (random.nextFloat() - 0.5f) * 0.2f), 
                    clamp.apply(0.7f + (random.nextFloat() - 0.5f) * 0.2f), 
                    clamp.apply(0.6f + (random.nextFloat() - 0.5f) * 0.2f)
                );
                tertiarySurface = Color.getHSBColor( 
                    clamp.apply(baseHueGas - 0.3f + (random.nextFloat() - 0.5f) * 0.2f), 
                    clamp.apply(0.5f + (random.nextFloat() - 0.5f) * 0.3f), 
                    clamp.apply(0.8f + (random.nextFloat() - 0.5f) * 0.15f)
                );
                hueShift = Color.getHSBColor( 
                    clamp.apply(baseHueGas + 0.5f + (random.nextFloat() - 0.5f) * 0.1f), 
                    clamp.apply(0.4f + (random.nextFloat() - 0.5f) * 0.2f), 
                    clamp.apply(0.65f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                rock = Color.getHSBColor( 
                    random.nextFloat() * 0.1f, 
                    clamp.apply(random.nextFloat() * 0.1f), 
                    clamp.apply(0.1f + (random.nextFloat() - 0.5f) * 0.1f)
                );
                break;
            default:

                primarySurface = Color.getHSBColor(random.nextFloat() * 0.1f, 0.1f, 0.5f + (random.nextFloat() -0.5f) * 0.2f);
                primaryLiquid = Color.getHSBColor(random.nextFloat() * 0.1f, 0.1f, 0.4f + (random.nextFloat() -0.5f) * 0.2f);
                secondarySurface = Color.getHSBColor(random.nextFloat() * 0.1f, 0.1f, 0.6f + (random.nextFloat() -0.5f) * 0.2f);
                tertiarySurface = Color.getHSBColor(random.nextFloat() * 0.1f, 0.1f, 0.3f + (random.nextFloat() -0.5f) * 0.2f);
                hueShift = Color.getHSBColor(random.nextFloat() * 0.1f, 0.1f, 0.5f + (random.nextFloat() -0.5f) * 0.1f);
                rock = Color.getHSBColor(random.nextFloat() * 0.1f, 0.05f, 0.2f + (random.nextFloat() -0.5f) * 0.1f);
                break;
        }

        return new ColorPallete(primarySurface, primaryLiquid, secondarySurface, tertiarySurface, hueShift, rock);
    }
    


    /**
     * Get the name of the planet
     * @return the name of the planet
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type of the planet
     * @return the type of the planet
     */
    public PlanetType getType() {
        return type;
    }

    /**
     * Get the width of the planet
     * @return the width of the planet
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of the planet
     * @return the height of the planet
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the gravity of the planet
     * @return the gravity of the planet
     */
    public double getGravity() {
        return gravity;
    }

    /**
     * Get the temperature of the planet
     * @return the temperature of the planet
     */
    public double getTempeature() {
        return tempeature;
    }

    /**
     * Get the seed used for generating the planet
     * @return the seed used for generating the planet
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Get the color palette of the planet
     * @return the color palette of the planet
     */
    public ColorPallete getColorPallete() {
        return colorPallete;
    }


    public int getPlanetId() {
        return planetId;
    }

    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }

    public int getGalaxyX() {
        return galaxyX;
    }

    public void setGalaxyX(int galaxyX) {
        this.galaxyX = galaxyX;
    }

    public int getGalaxyY() {
        return galaxyY;
    }

    public void setGalaxyY(int galaxyY) {
        this.galaxyY = galaxyY;
    }

    public List<Moon> getMoons() {
        return moons;
    }

    public void setMoons(List<Moon> moons) {
        this.moons = moons;
    }

    public void addMoon(Moon moon) {
        if (this.moons == null) {
            this.moons = new ArrayList<>();
        }
        this.moons.add(moon);
    }

    /**
     * Retrieves a list of solid tiles that are near a given bounding box.
     * This is used for collision detection with the environment.
     *
     * @param entityX      The x-coordinate of the entity's top-left corner.
     * @param entityY      The y-coordinate of the entity's top-left corner.
     * @param entityWidth  The width of the entity.
     * @param entityHeight The height of the entity.
     * @param searchRadius Additional radius around the entity to search for tiles (in tile units).
     * @return A list of solid Tile objects near the entity.
     */
    public List<Tile> getNearbySolidTiles(int entityX, int entityY, int entityWidth, int entityHeight, int searchRadius) {
        List<Tile> solidTiles = new ArrayList<>();
        
        int minTileX = (entityX / Tile.TILE_WIDTH) - searchRadius;
        int maxTileX = ((entityX + entityWidth) / Tile.TILE_WIDTH) + searchRadius;
        int minTileY = (entityY / Tile.TILE_HEIGHT) - searchRadius;
        int maxTileY = ((entityY + entityHeight) / Tile.TILE_HEIGHT) + searchRadius;

        for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
            for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
                int chunkX = Math.floorDiv(tileX, Chunk.CHUNK_WIDTH);
                int chunkY = Math.floorDiv(tileY, Chunk.CHUNK_HEIGHT);
                
                int localX = tileX % Chunk.CHUNK_WIDTH;
                if (localX < 0) localX += Chunk.CHUNK_WIDTH; 

                int localY = tileY % Chunk.CHUNK_HEIGHT;
                if (localY < 0) localY += Chunk.CHUNK_HEIGHT; 

                Chunk chunk = getChunk(chunkX, chunkY); 
                if (chunk != null) {
                    Tile tile = chunk.getTileAtLocal(localX, localY);
                    if (tile != null && tile.isSolid()) {
                   
                        solidTiles.add(tile);
                    }
                }
            }
        }
        return solidTiles;
    }


    /**
     * Formats the Planet data into a string for client communication.
     * Format: PLANET:<planetId>:<name>:<type_str>:<size_approx>:<color_rgb_primary>:<galaxyX>:<galaxyY>|M:<moon_data_1>;<moon_data_2>|...
     * @return A string representation of the planet for the client.
     */
    public String toClientStringFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("PLANET:");
        sb.append(planetId).append(":");
        sb.append(name).append(":");
        sb.append(type != null ? type.name() : "UNKNOWN").append(":");
        sb.append(width).append(":"); 

        if (colorPallete != null && colorPallete.getPrimarySurface() != null) {
            Color primaryColor = colorPallete.getPrimarySurface();
            sb.append(primaryColor.getRed()).append(",").append(primaryColor.getGreen()).append(",").append(primaryColor.getBlue());
        } else {
            sb.append("128,128,128"); 
        }
        sb.append(":").append(galaxyX);
        sb.append(":").append(galaxyY);

        sb.append(":");
        if (colorPallete != null && colorPallete.getHueShift() != null) {
            Color hueShiftColor = colorPallete.getHueShift();
            sb.append(hueShiftColor.getRed()).append(",").append(hueShiftColor.getGreen()).append(",").append(hueShiftColor.getBlue());
        } else {
            sb.append("128,128,128"); 
        }

        if (moons != null && !moons.isEmpty()) {
            sb.append("|M:");
            for (int i = 0; i < moons.size(); i++) {
                Moon moon = moons.get(i);
                sb.append(moon.toClientStringFormat()); 
                if (i < moons.size() - 1) {
                    sb.append(";");
                }
            }
        }
        return sb.toString();
    }
}
