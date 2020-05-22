package uibk.ac.at.prodiga.configs;

import com.google.common.collect.Lists;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.services.LogInformationService;
import uibk.ac.at.prodiga.services.RaspberryPiService;
import uibk.ac.at.prodiga.utils.JwtTokenUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PreAuthRequestFilter extends OncePerRequestFilter {

    private List<String> allowedApiRequests = Lists.newArrayList("/api/auth", "/api/register");

    @Autowired
    RaspberryPiService raspberryPiService;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    LogInformationService logInformationService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        boolean handleRequest = httpServletRequest.getRequestURI().startsWith("/api") &&
                allowedApiRequests.stream()
                    .noneMatch(x -> httpServletRequest.getRequestURI().startsWith(x));

        if(handleRequest) {
            String requestTokenHeader = httpServletRequest.getHeader("Authorization");
            String internalId = null;
            String jwtToken = null;

            // Jwt has form "Bearer <token>" -> remove prefix
            if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                jwtToken = requestTokenHeader.substring(7);
                try {
                    internalId = jwtTokenUtil.getInternalIdFromToken(jwtToken);
                } catch (IllegalArgumentException e) {
                    System.out.println("Unable to get JWT Token");
                } catch (ExpiredJwtException e) {
                    System.out.println("JWT Token has expired");
                }
            } else {
                logInformationService.logForRaspi("Request Token invalid, token was: " + requestTokenHeader, null);
            }
            // Once we get the token validate it.

            boolean success = false;

            if (internalId != null) {
                RaspberryPi raspberryPi = null;
                try {
                    raspberryPi = raspberryPiService.findByInternalIdAndThrow(internalId);
                } catch (Exception e) {
                    logInformationService.logForRaspi("Request Token contains InternalID " + internalId + " but no RaspberryPi was found", null);
                }

                // if token is valid configure Spring Security to manually set
                // authentication
                if (jwtTokenUtil.validateToken(jwtToken, raspberryPi)) {
                    httpServletRequest.authenticate(httpServletResponse);
                    success = true;
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(raspberryPi.getInternalId(), null, new ArrayList<>()));
                }
            }

            if(!success) {
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token not found!");
            } else {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            }
        } else {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }

    }
}
