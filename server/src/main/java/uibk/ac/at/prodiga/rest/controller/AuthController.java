package uibk.ac.at.prodiga.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.rest.dtos.JwtRequestDTO;
import uibk.ac.at.prodiga.rest.dtos.JwtResponseDTO;
import uibk.ac.at.prodiga.services.RaspberryPiService;
import uibk.ac.at.prodiga.utils.Constants;
import uibk.ac.at.prodiga.utils.JwtTokenUtil;

import javax.validation.Valid;
import java.util.Optional;

@RestController
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final RaspberryPiService raspberryPiService;

    public AuthController(JwtTokenUtil jwtTokenUtil, RaspberryPiService raspberryPiService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.raspberryPiService = raspberryPiService;
    }

    /**
     * Registers a new Raspberry Pi and adds it to the list of pending Raspberry Pis
     * @param internalId The internal Id used by the Raspberry Pi.
     */
    @PostMapping("/api/register")
    public void register(String internalId) {
        Optional<RaspberryPi> existing = raspberryPiService.findByInternalId(internalId);

        if(existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A raspi with the same internalId already exists");
        }

        if(!raspberryPiService.tryAddPendingRaspberry(internalId)){
            throw new ResponseStatusException(HttpStatus.CONTINUE, "Raspi already ready added to config list");
        }
    }

    /**
     * Creates a Jwt request token for the given Raspberry Pi.
     * @param request A Jwt Request Object containing internal Id and password.
     * @return The generated token.
     */
    @PostMapping("/api/auth")
    public JwtResponseDTO createToken(@Valid @RequestBody JwtRequestDTO request) {
        RaspberryPi raspi;

        try {
            raspi = raspberryPiService.findByInternalIdAndThrow(request.getInternalId());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Raspi " +
                    "not found", ex);
        }

        if(!Constants.PASSWORD_ENCODER.matches(request.getPassword(), raspi.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password" +
                    " does not match");
        }

        try {
            return new JwtResponseDTO(jwtTokenUtil.generateToken(raspi));
        }catch (Exception ex) {
            // Ignore
            return null;
        }
    }
}
