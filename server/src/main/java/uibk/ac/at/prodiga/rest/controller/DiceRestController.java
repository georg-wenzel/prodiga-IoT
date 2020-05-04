package uibk.ac.at.prodiga.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.rest.dtos.HistoryEntryDTO;
import uibk.ac.at.prodiga.rest.dtos.NewDiceSideRequestDTO;
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.services.DiceSideService;
import uibk.ac.at.prodiga.utils.Constants;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
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
        if(diceService.getDiceByInternalId(request.getInternalId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Dice with internalId "+ request.getInternalId() + " not found!");
        }

        if(diceService.diceInConfigurationMode(request.getInternalId())) {
            diceService.onNewDiceSide(request.getInternalId(), request.getSide());
        }
    }

    /**
     * Service call for clients to add or update their booking entries
     * @param historyEntries A list with entries containing information about the dice,
     *                       the side and the TOTAL seconds on this side
     */
    @PostMapping("api/booking")
    public void addBooking(@Valid @RequestBody List<HistoryEntryDTO> historyEntries) {
        // First we create a list with rappers which filter all invalid entries
        List<HistoryEntryWrapper> realEntries = historyEntries.stream()
                .map(x -> new HistoryEntryWrapper(x, diceService, diceSideService))
                .filter(x -> x.handleEntry).collect(Collectors.toList()) ;

        // Iterate over all filtered entries and create booking - here we know all data is valid
        for(HistoryEntryWrapper entry : realEntries){
            Dice dice = entry.dice;
            BookingCategory bookingCategory = entry.bookingCategory;
            User user = entry.user;
            int seconds = entry.entry.getSeconds();

            // First we check the last users booking
            Booking lastBooking = bookingService.getLastBookingForDice(dice);

            Instant newStartDate = null;

            // Depending on if there is already a booking we set the start date

            // If there is already a booking we have to get the endDate and add the current seconds
            // in order to get the new Start date
            if(lastBooking != null) {
                newStartDate = lastBooking.getActivityEndDate().toInstant();
            } else {
                // If there is no booking we have to get all current history entries for the given dice
                // Sum up the seconds to get the real start date

                int sumSeconds = realEntries.stream().
                        filter(x -> x.dice.getInternalId().equals(dice.getInternalId()))
                        .mapToInt(x -> x.entry.getSeconds())
                        .sum();

                // Now we have to subtract the summed seconds from the current time
                newStartDate = Instant.now().minus(Duration.ofMinutes(sumSeconds));
            }

            // The end Date stays the same (independent of existing or non existing bookings)
            // It's just the start date + the seconds
            Instant newEndDate = newStartDate.plus(Duration.ofSeconds(seconds));

            Booking b = new Booking();
            b.setDice(dice);
            b.setActivityEndDate(Date.from(newEndDate));
            b.setActivityStartDate(Date.from(newStartDate));
            b.setBookingCategory(bookingCategory);

            try {
                bookingService.saveBooking(b, user, false);
            } catch (Exception ex) {
                // Ignore - can't do anything against it anyways
            }
        }
    }

    private static class HistoryEntryWrapper {

        private final HistoryEntryDTO entry;
        private final DiceService diceService;
        private final DiceSideService diceSideService;

        private Dice dice;
        private BookingCategory bookingCategory;
        private User user;
        private boolean handleEntry = false;

        private HistoryEntryWrapper(HistoryEntryDTO entry,
                                    DiceService diceService,
                                    DiceSideService diceSideService) {
            this.entry = entry;
            this.diceService = diceService;
            this.diceSideService = diceSideService;
            this.handleEntry = getHandleEntry();
        }

        private boolean getHandleEntry() {
            dice = diceService.getDiceByInternalId(entry.getCubeInternalId());

            if(dice == null) {
                // Dice seems not to be registered here - so ignore this entry
                return false;
            }

            user = dice.getUser();

            if(user == null) {
                // Seems like the dice doesn't have a user assigned - ignore
                return false;
            }

            DiceSide diceSide = diceSideService.findByDiceAndSide(dice, entry.getSide());

            if(diceSide == null) {
                // The given side is not configured on the given dice - so ignore this entry
                return false;
            }

            bookingCategory = diceSide.getBookingCategory();

            if(bookingCategory == null) {
                // In theory this should never happen - dice side without BookingCategory is not valid.
                // We don't want any exceptions here so ignore this entry
                return false;
            }

            // If so the dice was on side vacation our out of office or something
            // Ignore this entry
            return !bookingCategory.getId().equals(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID);
        }
    }

}
