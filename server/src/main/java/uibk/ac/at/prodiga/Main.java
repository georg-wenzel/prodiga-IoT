package uibk.ac.at.prodiga;

import java.util.HashMap;
import javax.faces.webapp.FacesServlet;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uibk.ac.at.prodiga.configs.CustomServletContextInitializer;
import uibk.ac.at.prodiga.configs.WebSecurityConfig;
import uibk.ac.at.prodiga.utils.ViewScope;

import static io.swagger.models.auth.In.HEADER;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableSwagger2
public class Main extends SpringBootServletInitializer {

    public static void main(String[] args) { //NOSONAR
        // Spring  should check the arguments internally
        // - they are not used most of the time anyways
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

    @Bean
    public Docket prodigaApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .securitySchemes(singletonList(new ApiKey("JWT", AUTHORIZATION, HEADER.name())))
                .securityContexts(singletonList(
                        SecurityContext.builder()
                                .securityReferences(
                                        singletonList(SecurityReference.builder()
                                                .reference("JWT")
                                                .scopes(new AuthorizationScope[0])
                                                .build()
                                        )
                                )
                                .build())
                )
                .select()
                    .apis(RequestHandlerSelectors
                        .basePackage("uibk.ac.at.prodiga.rest.controller"))
                    .paths(PathSelectors.regex("/.*"))
                .build().apiInfo(new ApiInfoBuilder()
                        .title("Prodiga REST API")
                        .description("Productivity Information Gateway REST API")
                        .license("")
                        .version("1.0.0").build());

    }

}
