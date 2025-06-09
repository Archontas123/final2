package com.tavuc.ecs;

import com.tavuc.ecs.components.Component;
import java.util.HashMap;
import java.util.Map;

// This class was mainly obtained from a stackoverflow post and then adapted for my purposes
/**
 * A container for managing a collection of {@link Component} objects for an entity.
 */
public class ComponentContainer {
    /**
     * The internal map storing components, keyed by their class type.
     */
    private final Map<Class<? extends Component>, Component> components = new HashMap<>();
    
    /**
     * Adds a component to the container. If a component of the same type already
     * exists, it will be replaced.
     * @param <T> The type of the component.
     * @param component The component instance to add.
     */
    public <T extends Component> void addComponent(T component) {
        components.put(component.getClass(), component);
    }
    
    /**
     * Retrieves a component of the specified class type from the container.
     * @param <T> The type of the component to retrieve.
     * @param componentClass The class object of the component to retrieve.
     * @return The component instance if it exists, or {@code null} otherwise.
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return (T) components.get(componentClass);
    }
    
    /**
     * Checks if the container has a component of the specified class type.
     * @param <T> The type of the component to check for.
     * @param componentClass The class object of the component to check for.
     * @return {@code true} if a component of the specified type exists, {@code false} otherwise.
     */
    public <T extends Component> boolean hasComponent(Class<T> componentClass) {
        return components.containsKey(componentClass);
    }
    
    /**
     * Removes a component of the specified class type from the container.
     * @param <T> The type of the component to remove.
     * @param componentClass The class object of the component to remove.
     */
    public <T extends Component> void removeComponent(Class<T> componentClass) {
        components.remove(componentClass);
    }
    
    /**
     * Removes all components from this container.
     */
    public void clearComponents() {
        components.clear();
    }
}