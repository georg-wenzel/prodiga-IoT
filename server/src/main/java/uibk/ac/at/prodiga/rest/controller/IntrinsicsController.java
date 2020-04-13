package uibk.ac.at.prodiga.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uibk.ac.at.prodiga.rest.dtos.GenericStringDTO;
import uibk.ac.at.prodiga.rest.dtos.InstrincsDTO;

@RestController
public class IntrinsicsController {

    @GetMapping("/api/ping")
    public GenericStringDTO ping() {
        GenericStringDTO dto = new GenericStringDTO();
        dto.setResponse("Pong \uD83D\uDC1D");
        return dto;
    }

    @PostMapping("/api/instrincs")
    public void push(@RequestBody InstrincsDTO instrincs) {

    }
}
