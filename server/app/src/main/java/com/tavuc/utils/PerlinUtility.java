package com.tavuc.utils;

import java.util.Random;

public class PerlinUtility {

    private static final int P = 512;
    private final int[] permutation = new int[P];
    private final int[] p = new int[P / 2];

    /**
     * Constructor for PerlinUtility
     * @param seed the seed for random number generation
     */
    public PerlinUtility(long seed) {
        Random random = new Random(seed);
        for (int i = 0; i < P / 2; i++) {
            p[i] = i;
        }

        for (int i = P / 2 - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = p[index];
            p[index] = p[i];
            p[i] = temp;
        }

        for (int i = 0; i < P / 2; i++) {
            permutation[i] = p[i];
            permutation[i + P / 2] = p[i];
        }
    }

    /**
     * Generates Perlin noise for the given coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the Perlin noise value at (x, y)
     */
    public double noise(double x, double y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);

        double u = fade(x);
        double v = fade(y);

        int A = permutation[X] + Y;
        int B = permutation[X + 1] + Y;

        return lerp(v,
                lerp(u, grad(permutation[A], x, y), grad(permutation[B], x - 1, y)),
                lerp(u, grad(permutation[A + 1], x, y - 1), grad(permutation[B + 1], x - 1, y - 1)));
    }

    /**
     * Fade function as defined by Ken Perlin. This eases coordinate values
     * @param t the coordinate value to fade
     * @return the faded value
     */
    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /**
     * Linear interpolation function
     * @param t the interpolation factor
     * @param a the start value
     * @param b the end value
     * @return the interpolated value
     */
    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    /**
     * Gradient function to compute the gradient based on the hash value and coordinates
     * @param hash the hash value
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the gradient value
     */
    private static double grad(int hash, double x, double y) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : 0;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    /**
     * Generates octave noise using Perlin noise.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param octaves the number of octaves to combine
     * @param persistence the amplitude reduction factor for each octave
     * @return the combined noise value
     */
    public double octaveNoise(double x, double y, int octaves, double persistence) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        double maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        return total / maxValue;
    }
}
