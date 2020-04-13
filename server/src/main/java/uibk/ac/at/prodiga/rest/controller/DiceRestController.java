package uibk.ac.at.prodiga.rest.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uibk.ac.at.prodiga.rest.dtos.NewDiceSideRequestDTO;
import uibk.ac.at.prodiga.services.DiceService;

import javax.validation.Valid;

@RestController
@CrossOrigin
public class DiceRestController {

    private final DiceService diceService;

    public DiceRestController(DiceService diceService) {
        this.diceService = diceService;
    }

    @PostMapping("api/newSide")
    public void notifyNewSide(@Valid @RequestBody NewDiceSideRequestDTO request) {
        if(diceService.diceInConfigurationMode(request.getInternalId())) {
            diceService.onNewDiceSide(request.getInternalId(), request.getSide());
        } else {
            //TODO Max: Booking stuff
        }
    }

}
