package aston.hw2.context;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractObjectContainerBuilder implements ObjectContainerBuilder {

    private final Map<Class<?>, Object> container = new HashMap<>();

    protected <T> void add(T t, Class<? super T> clazz) {
        if (t == null) {
            throw new IllegalArgumentException("An objet must not be null");
        }

        container.put(clazz, t);
    }

    @Override
    public Map<Class<?>, Object> build() {
        configure();
        return Map.copyOf(container);
    }

    protected abstract void configure();

    @SuppressWarnings("unchecked")
    protected <T> T get(Class<? extends T> clazz) {
        return (T) container.get(clazz);
    }
}
