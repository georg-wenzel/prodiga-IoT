package uibk.ac.at.prodiga.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.DiceSide;
import uibk.ac.at.prodiga.rest.dtos.HistoryEntryDTO;
import uibk.ac.at.prodiga.rest.dtos.NewDiceSideRequestDTO;
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.services.DiceSideService;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import javax.validation.Valid;
import java.util.List;

@RestController
@CrossOrigin
public class DiceRestController {

    private final DiceService diceService;
    private final DiceSideService diceSideService;
    private final BookingService bookingService;

    public DiceRestController(DiceService diceService, DiceSideService diceSideService, BookingService bookingService) {
        this.diceService = diceService;
        this.diceSideService = diceSideService;
        this.bookingService = bookingService;
    }

    @PostMapping("api/newSide")
    public void notifyNewSide(@Valid @RequestBody NewDiceSideRequestDTO request) {
        if(diceService.diceInConfigurationMode(request.getInternalId())) {
            diceService.onNewDiceSide(request.getInternalId(), request.getSide());
        } else {
            //TODO Max: Booking stuff
        }
    }

    @PostMapping("api/booking")
    public void addBooking(@Valid @RequestBody List<HistoryEntryDTO> historyEntryDTO) {
       for(HistoryEntryDTO historyEntryDTO1: historyEntryDTO){
           Booking booking = new Booking();
           Dice dice =  diceService.getDiceByInternalId(historyEntryDTO1.getCubeInternalId());

           if(dice == null){
               throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dice not found");
           }

           DiceSide diceSide = diceSideService.findByDiceAndSide(dice, historyEntryDTO1.getSide());

           if(diceSide == null){
               throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DiceSide not found");
           }

           BookingCategory bookingCategory = diceSide.getBookingCategory();

           booking.setBookingCategory(bookingCategory);
           booking.setDice(dice);

           try {
               bookingService.saveBooking(booking);
           }
           catch (Exception ex){
               throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.toString());
           }
       }
    }

}
