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

@RestController
@CrossOrigin
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final RaspberryPiService raspberryPiService;

    public AuthController(JwtTokenUtil jwtTokenUtil, RaspberryPiService raspberryPiService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.raspberryPiService = raspberryPiService;
    }


    @PostMapping("/api/auth")
    public JwtResponseDTO createToken(@Valid @RequestBody JwtRequestDTO request) {
        RaspberryPi raspi;

        try {
            raspi = raspberryPiService.findByInternalIdAndThrow(request.getInternalId());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Raspi " +
                    "not found", ex);
        }

        if(!Constants.PASSWORD_ENCODER.encode(request.getPassword())
                .equals(raspi.getPassword())) {
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
