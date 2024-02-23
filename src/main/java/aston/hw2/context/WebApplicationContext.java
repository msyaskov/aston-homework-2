package aston.hw2.context;

import aston.hw2.configuration.ProductionObjectContainerBuilder;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.Setter;

import java.util.Map;
import java.util.NoSuchElementException;

@WebListener("WebApplicationContext")
public class WebApplicationContext implements ServletContextListener {

    private Map<Class<?>, Object> container;

    @Setter
    private ObjectContainerBuilder objectContainerBuilder = new ProductionObjectContainerBuilder();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        container = objectContainerBuilder.build();
        sce.getServletContext().setAttribute(WebApplicationContext.class.getName(), this);
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<T> clazz) { // а вот и реализация сервис локатора, как раз его же и обсуждали
        if (clazz == null) {
            throw new IllegalStateException("A clazz must not be null");
        }

        Object object = container.get(clazz);
        if (object == null) {
            throw new NoSuchElementException("No instance found for class " + clazz.getName());
        }

        return (T) object;
    }
}
