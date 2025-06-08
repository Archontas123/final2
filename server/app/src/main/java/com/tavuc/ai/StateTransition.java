package com.tavuc.ai;

import java.util.function.Supplier;

/**
 * Describes a possible transition between AI states on the server.
 */
public class StateTransition {
    private final AIState nextState;
    private final Supplier<Boolean> condition;

    public StateTransition(AIState nextState, Supplier<Boolean> condition) {
        this.nextState = nextState;
        this.condition = condition;
    }

    public AIState getNextState() {
        return nextState;
    }

    public boolean shouldTransition() {
        return condition.get();
    }
}
