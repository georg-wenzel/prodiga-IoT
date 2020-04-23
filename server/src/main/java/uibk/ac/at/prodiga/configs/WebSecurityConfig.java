package uibk.ac.at.prodiga.configs;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import uibk.ac.at.prodiga.utils.Constants;

@Configuration
@EnableWebSecurity()
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    DataSource dataSource;

    @Autowired
    PreAuthRequestFilter preAuthRequestFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // TODO(Max) Check if we need this
        // http.csrf().disable();

        http.headers().frameOptions().disable(); // needed for H2 console

        http.logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/login.xhtml");

        http.authorizeRequests()
                .antMatchers("/h2-console/**", "/api/auth", "/api/register").permitAll();

        http.authorizeRequests()
                .and()
                .exceptionHandling().accessDeniedPage("/error/denied.xhtml")
                .and()
                .sessionManagement().invalidSessionUrl("/error/invalid_session.xhtml")
                .and()
                .formLogin()
                .loginPage("/login.xhtml")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/welcome.xhtml")
                .failureUrl("/login.xhtml?error=true");

        http.authorizeRequests()
                .antMatchers("/api/**").authenticated();

        http.addFilterBefore(preAuthRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        //Configure roles and passwords via datasource
        // Sonar claims we use a plan text Password encoder which is not the case
        // See below
        auth.jdbcAuthentication().dataSource(dataSource) //NOSONAR
                .usersByUsernameQuery("select username, password, enabled from user where username=?")
                .authoritiesByUsernameQuery("select user_username, roles from user_user_role where user_username=?");
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return Constants.PASSWORD_ENCODER;
    }
}