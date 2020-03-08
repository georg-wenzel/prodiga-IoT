package uibk.ac.at.prodiga;

import java.util.HashMap;
import javax.faces.webapp.FacesServlet;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import uibk.ac.at.prodiga.configs.CustomServletContextInitializer;
import uibk.ac.at.prodiga.configs.WebSecurityConfig;
import uibk.ac.at.prodiga.utils.ViewScope;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class Main extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Main.class, CustomServletContextInitializer.class, WebSecurityConfig.class);
    }

    @Bean
    public ServletRegistrationBean<FacesServlet> servletRegistrationBean() {
        FacesServlet servlet = new FacesServlet();
        return new ServletRegistrationBean<>(servlet, "*.xhtml");
    }

    @Bean
    public CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
        HashMap<String, Object> customScopes = new HashMap<>();
        customScopes.put("view", new ViewScope());
        customScopeConfigurer.setScopes(customScopes);
        return customScopeConfigurer;
    }

}
