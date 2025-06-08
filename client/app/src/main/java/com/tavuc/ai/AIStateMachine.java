package com.tavuc.ai;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Simple finite state machine for enemy AI.
 */
public class AIStateMachine {
    private AIState currentState = AIState.SPAWNING;
    private final Map<AIState, List<StateTransition>> transitions = new EnumMap<>(AIState.class);

    public AIState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(AIState state) {
        this.currentState = state;
    }

    public void addTransition(AIState from, StateTransition transition) {
        transitions.computeIfAbsent(from, k -> new ArrayList<>()).add(transition);
    }

    public void update() {
        List<StateTransition> ts = transitions.get(currentState);
        if (ts == null) return;
        for (StateTransition t : ts) {
            if (t.shouldTransition()) {
                currentState = t.getNextState();
                break;
            }
        }
    }
}
