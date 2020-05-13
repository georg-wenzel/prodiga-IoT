package uibk.ac.at.prodiga.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.DiceSide;
import uibk.ac.at.prodiga.repositories.DiceSideRepository;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Collection;
import java.util.Date;

@Component
@Scope("application")
public class DiceSideService {

    private final DiceSideRepository diceSideRepository;
    private final ProdigaUserLoginManager prodigaUserLoginManager;
    private final LogInformationService logInformationService;

    public DiceSideService(DiceSideRepository diceSideRepository, ProdigaUserLoginManager prodigaUserLoginManager, LogInformationService logInformationService) {
        this.diceSideRepository = diceSideRepository;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
        this.logInformationService = logInformationService;
    }

    /**
     * Saves the given DiceSide
     * @param ds The diceSide to save
     * @return The newly saved dice side
     */
    public DiceSide save(DiceSide ds) {
        if(ds.isNew()) {
            ds.setObjectCreatedDateTime(new Date());
            ds.setObjectCreatedUser(prodigaUserLoginManager.getCurrentUser());
        } else {
            DiceSide dbDiceSide = diceSideRepository.findFirstByDiceAndSide(ds.getDice(), ds.getSide());

            if(dbDiceSide != null) {
                dbDiceSide.setBookingCategory(ds.getBookingCategory());
                ds = dbDiceSide;
            }
            ds.setObjectChangedDateTime(new Date());
            ds.setObjectChangedUser(prodigaUserLoginManager.getCurrentUser());
        }

        DiceSide result = diceSideRepository.save(ds);

        logInformationService.logForCurrentUser("DiceSide " + result.getId() + " saved");

        return result;
    }

    /**
     * Deletes the given dice side
     * @param ds The dice side
     */
    public void delete(DiceSide ds) {
        diceSideRepository.delete(ds);
        logInformationService.logForCurrentUser("DiceSide " + ds.getId() + " deleted");
    }

    /**
     * Handles when a dice side gets configured
     * @param side The side on the dice
     * @param category The assigned category
     * @param d The dice
     */
    public void onNewConfiguredDiceSide(int side, int sideFriendlyName, BookingCategory category, Dice d) {
        DiceSide exiting = findByDiceAndSide(d, side);

        logInformationService.logForCurrentUser("New Dice Side " + side + " for dice " + d.getInternalId());

        // If no existing dice side and we got a category -> create new one
        if(exiting == null && category != null) {
            DiceSide newDiceSide = new DiceSide();
            newDiceSide.setSide(side);
            newDiceSide.setBookingCategory(category);
            newDiceSide.setDice(d);
            newDiceSide.setSideFriendlyName(sideFriendlyName);

            save(newDiceSide);
        } else if(exiting != null && category != null) {
            // We got a dice side and a category - set the category and the new friendly name to this side
            exiting.setBookingCategory(category);
            exiting.setSideFriendlyName(sideFriendlyName);
            save(exiting);
        } else if(exiting != null){
            // He the category is not set - but we have an existing side - so delete this side
            delete(exiting);
        }
        // else -> no existing side and no category - so ignore
    }

    public DiceSide findByDiceAndSide(Dice dice, Integer side){
        return diceSideRepository.findFirstByDiceAndSide(dice, side);
    }

    public Collection<DiceSide> findByDice(Dice dice)
    {
        return diceSideRepository.findAllByDice(dice);
    }
}
