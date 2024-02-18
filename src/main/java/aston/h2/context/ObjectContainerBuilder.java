package aston.h2.context;

import java.util.Map;

public interface ObjectContainerBuilder {

    Map<Class<?>, Object> build();

}
