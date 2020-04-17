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
import uibk.ac.at.prodiga.utils.Constants;

import javax.validation.Valid;
import java.util.Date;
import java.time.Duration;
import java.time.Instant;
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

    /**
     * Service call for clients to notify the server the current side on the dice has changed
     * @param request The HTTP request containing information about dice and current side
     */
    @PostMapping("api/newSide")
    public void notifyNewSide(@Valid @RequestBody NewDiceSideRequestDTO request) {
        if(diceService.diceInConfigurationMode(request.getInternalId())) {
            diceService.onNewDiceSide(request.getInternalId(), request.getSide());
        } else {
            //TODO Max: Booking stuff
        }
    }

    /**
     * Service call for clients to add or update their booking entries
     * @param historyEntries A list with entries containing information about the dice,
     *                       the side and the TOTAL seconds on this side
     */
    @PostMapping("api/booking")
    public void addBooking(@Valid @RequestBody List<HistoryEntryDTO> historyEntries) {
        // Iterate over all entries we got from the client
        for(HistoryEntryDTO entry: historyEntries){
            // First lets get the dice
            Dice dice =  diceService.getDiceByInternalId(entry.getCubeInternalId());

            // Return 404 in case we didn't find anything
            if(dice == null){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dice not found");
            }

            // Get the dice side next
            DiceSide diceSide = diceSideService.findByDiceAndSide(dice, entry.getSide());

            // If there is no side configured on this cube we can't do anything here
            // Side needs configuration first
            if(diceSide == null){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DiceSide not found");
            }

            // Now we get the last booking which was made for the given dice
            Booking existingBooking = bookingService.getLastBookingForDice(dice);

            BookingCategory bookingCategory = diceSide.getBookingCategory();

            // If we found a booking and the last booking was the same category
            // (the same side on the dice) we can update the end date
            if(existingBooking != null
                    && existingBooking.getBookingCategory().getId().equals(bookingCategory.getId())) {
                Instant endDate = existingBooking.getActivityEndDate().toInstant();
                // We add the different between the total seconds on the dice
                // and the last saved seconds to the existing end Time
                // So we get a new end Time
                Instant newEnd = endDate.plus(Duration.ofSeconds(entry.getSeconds() - diceSide.getCurrentSeconds()));

                // Update update the currentSeconds on the diceSide first - in case that fails
                // We do not modify the the booking and next them this method gets called
                // Everything is still the same

                // This right here btw is the most annoying fact about getter/setter
                // Get your properties java
                diceSide.setCurrentSeconds(diceSide.getCurrentSeconds() + entry.getSeconds());

                diceSideService.save(diceSide);

                // If we get so far we can update the booking and gtfo
                existingBooking.setActivityEndDate(Date.from(newEnd));

                saveBookingAndThrow(existingBooking);

                return;
            }

            // He we are when this is the first booking or if the last booking was a different type
            // In both cases we create a new booking
            // Unless we know its the special do not book category - where we just idle
            if (!bookingCategory.getId().equals(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID)) {
                Booking booking = new Booking();

                // Set dice and category
                booking.setBookingCategory(bookingCategory);
                booking.setDice(dice);

                // Create the start date
                // The start date is 15 minutes ago, this method gets called every 15 minutes
                // So we assume the last call was the moment when the user has turned his dice
                Instant startDate = new Date().toInstant().minus(Duration.ofMinutes(15));
                booking.setActivityStartDate(Date.from(startDate));
                
                // End Date is always now - so every booking has at least 15 minutes
                booking.setActivityEndDate(new Date());
                
                // Again, as above, we first update the dice side seconds
                diceSide.setCurrentSeconds(diceSide.getCurrentSeconds() + entry.getSeconds());

                diceSideService.save(diceSide);

                // Now we can save the booking - remember order is important here
                saveBookingAndThrow(booking);
            }
        }
    }

    private void saveBookingAndThrow(Booking b) {
        try {
            bookingService.saveBooking(b);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.toString());
        }
    }

}
