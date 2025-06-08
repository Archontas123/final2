package com.tavuc.controllers;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Comparator;
import com.tavuc.utils.Vector2D;

/**
 * Buffers recent player inputs to allow responsive command processing.
 */
public class InputBuffer {
    public enum KeyBinding {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        SLIDE,
        DODGE
    }

    public static class InputCommand {
        public final KeyBinding binding;
        public final long timestamp;
        public InputCommand(KeyBinding binding, long timestamp) {
            this.binding = binding;
            this.timestamp = timestamp;
        }
    }

    private final Deque<InputCommand> commandQueue = new ArrayDeque<>();
    private final Map<KeyBinding, Long> lastInputTimes = new EnumMap<>(KeyBinding.class);
    private final Map<KeyBinding, Integer> priorityMap = new EnumMap<>(KeyBinding.class);
    private final long bufferWindowMillis;

    public InputBuffer(long bufferWindowMillis) {
        this.bufferWindowMillis = bufferWindowMillis;
        // default priorities: movement > weapon > ability
        priorityMap.put(KeyBinding.MOVE_UP, 3);
        priorityMap.put(KeyBinding.MOVE_DOWN, 3);
        priorityMap.put(KeyBinding.MOVE_LEFT, 3);
        priorityMap.put(KeyBinding.MOVE_RIGHT, 3);
        priorityMap.put(KeyBinding.SLIDE, 2);
        priorityMap.put(KeyBinding.DODGE, 2);
    }

    /** Registers an input event for the given binding. */
    public void registerInput(KeyBinding binding) {
        long now = System.currentTimeMillis();
        commandQueue.addLast(new InputCommand(binding, now));
        lastInputTimes.put(binding, now);
    }

    /**
     * Returns true if the given binding was triggered within the buffer window.
     */
    public boolean isActive(KeyBinding binding) {
        Long lastTime = lastInputTimes.get(binding);
        if (lastTime == null) return false;
        return System.currentTimeMillis() - lastTime <= bufferWindowMillis;
    }

    /** Removes expired commands from the buffer. */
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        while (!commandQueue.isEmpty() && now - commandQueue.peekFirst().timestamp > bufferWindowMillis) {
            commandQueue.pollFirst();
        }
    }

    /** Returns the most recent high priority command in the buffer. */
    public KeyBinding peekHighestPriority() {
        purgeExpired();
        return commandQueue.stream()
                .max(Comparator.comparingInt(c -> priorityMap.getOrDefault(c.binding, 0)))
                .map(c -> c.binding)
                .orElse(null);
    }

    /** Returns a predicted movement direction based on recent inputs. */
    public Vector2D getPredictedDirection() {
        purgeExpired();
        double x = 0, y = 0;
        if (isActive(KeyBinding.MOVE_UP)) y -= 1;
        if (isActive(KeyBinding.MOVE_DOWN)) y += 1;
        if (isActive(KeyBinding.MOVE_LEFT)) x -= 1;
        if (isActive(KeyBinding.MOVE_RIGHT)) x += 1;
        Vector2D v = new Vector2D(x, y);
        if (v.length() > 0) v.normalize();
        return v;
    }
}
