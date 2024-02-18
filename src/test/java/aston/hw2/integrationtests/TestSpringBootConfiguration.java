package aston.hw2.integrationtests;

import aston.hw2.configuration.ProductionObjectContainerBuilder;
import aston.hw2.context.ObjectContainerBuilder;
import aston.hw2.context.WebApplicationContext;
import aston.hw2.repository.JdbcConnectionFactory;
import aston.hw2.repository.TestJdbcHelper;
import aston.hw2.servlet.CuratorServlet;
import aston.hw2.servlet.GroupServlet;
import aston.hw2.servlet.StudentServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestSpringBootConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(TestSpringBootConfiguration.class, args);
    }

    @Bean
    public ServletRegistrationBean<GroupServlet> groupServletRegistrationBean() {
        ServletRegistrationBean<GroupServlet> registrationBean = new ServletRegistrationBean<>(new GroupServlet());
        registrationBean.setName("groupServlet");
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/groups", "/groups/*");
        return registrationBean;
    }

    @Bean
    public ServletRegistrationBean<StudentServlet> studentServletRegistrationBean() {
        ServletRegistrationBean<StudentServlet> registrationBean = new ServletRegistrationBean<>(new StudentServlet());
        registrationBean.setName("studentServlet");
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/students", "/students/*");
        return registrationBean;
    }

    @Bean
    public ServletRegistrationBean<CuratorServlet> curatorServletRegistrationBean() {
        ServletRegistrationBean<CuratorServlet> registrationBean = new ServletRegistrationBean<>(new CuratorServlet());
        registrationBean.setName("curatorServlet");
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/curators", "/curators/*");
        return registrationBean;
    }

    // Мой контекст, никак не связан с контекстом спринга
    @Bean
    public WebApplicationContext webApplicationContext() {
        WebApplicationContext webApplicationContext = new WebApplicationContext();
        webApplicationContext.setObjectContainerBuilder(testObjectContainerBuilder());
        return webApplicationContext;
    }

    private ObjectContainerBuilder testObjectContainerBuilder() {
        return new ProductionObjectContainerBuilder() {
            @Override
            protected void configure() {
                add(testJdbcConnectionFactory(), JdbcConnectionFactory.class);
                super.configure();
            }
        };
    }

    @Bean
    public JdbcConnectionFactory testJdbcConnectionFactory() {
        return new JdbcConnectionFactory("org.h2.Driver", "jdbc:h2:mem:test_db;DEFAULT_LOCK_TIMEOUT=10000;LOCK_MODE=0;DB_CLOSE_DELAY=-1");
    }

    @Bean
    public TestJdbcHelper testJdbcHelper() {
        return new TestJdbcHelper(testJdbcConnectionFactory());
    }
}
