package uibk.ac.at.prodiga.configs;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.utils.Constants;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    DataSource dataSource;

    @Autowired
    UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // Raspberry Pi is already authenticated based on the token
        if(authentication.getPrincipal() instanceof RaspberryPi) {
            RaspberryPi raspi = (RaspberryPi)authentication.getPrincipal();
            return new UsernamePasswordAuthenticationToken(raspi.getInternalId(), raspi.getPassword(), Lists.newArrayList(new SimpleGrantedAuthority("ADMIN")));
        } else if(authentication.getPrincipal() instanceof String) {
            // user have to be authenticated

            User u = userRepository.findFirstByUsername((String) authentication.getPrincipal());

            if (u == null) {
                throw new BadCredentialsException("No User found");
            }

            if (Constants.PASSWORD_ENCODER.matches((String) authentication.getCredentials(), u.getPassword())) {
                List<SimpleGrantedAuthority> auths = u.getRoles().stream().map(x -> new SimpleGrantedAuthority(x.toString())).collect(Collectors.toList());

                return new UsernamePasswordAuthenticationToken(u.getUsername(), u.getPassword(), auths);
            }
        }

        throw new BadCredentialsException("Unknown authentication!");
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
