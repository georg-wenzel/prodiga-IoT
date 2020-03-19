package uibk.ac.at.prodiga.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.RaspberryPi;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = 3482234902843298432L;
    private static final long JWT_TOKEN_VALIDITY = 3 * 60 * 60; // 3 hours

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(RaspberryPi raspberry) throws Exception {
        if(raspberry == null) {
            throw new ProdigaGeneralExpectedException("Raspberry may not be " +
                    "null", MessageType.ERROR);
        }

        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(raspberry.getInternalId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.ES256, secret).compact();
    }

    public boolean validateToken(String token, RaspberryPi raspberry) {
        if(raspberry == null) {
            return false;
        }
        return raspberry.getInternalId().equals(getInternalIdFromToken(token)) &&
                !isTokenExpered(token);
    }

    public String getInternalIdFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    private boolean isTokenExpered(String token) {
        Date exp = getClaimFromToken(token, Claims::getExpiration);
        return exp.before(new Date());
    }

    private <T> T getClaimFromToken(String token,
                                   Function<Claims, T> claimsFunction) {
        return claimsFunction.apply(Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody());
    }



}
