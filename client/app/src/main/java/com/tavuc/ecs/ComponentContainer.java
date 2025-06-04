package com.tavuc.ecs;

import com.tavuc.ecs.components.Component;
import java.util.HashMap;
import java.util.Map;

public class ComponentContainer {
    private final Map<Class<? extends Component>, Component> components = new HashMap<>();
    
    public <T extends Component> void addComponent(T component) {
        components.put(component.getClass(), component);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return (T) components.get(componentClass);
    }
    
    public <T extends Component> boolean hasComponent(Class<T> componentClass) {
        return components.containsKey(componentClass);
    }
    
    public <T extends Component> void removeComponent(Class<T> componentClass) {
        components.remove(componentClass);
    }
    
    public void clearComponents() {
        components.clear();
    }
}
