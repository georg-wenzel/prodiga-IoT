package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.services.BookingCategoryService;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing user dice configuration
 */
@Component
@Scope("view")
public class DiceConfigurationController
{
    private final ProdigaUserLoginManager userLoginManager;
    private final DiceService diceService;
    private final BookingCategoryService bookingCategoryService;

    private Dice dice;
    private boolean inConfiguration = false;

    private Map<Integer, DiceSide> sides;

    public DiceConfigurationController(ProdigaUserLoginManager userLoginManager, DiceService diceService, BookingCategoryService bookingCategoryService)
    {
        this.userLoginManager = userLoginManager;
        this.diceService = diceService;
        this.bookingCategoryService = bookingCategoryService;

        this.dice = this.diceService.getDiceByUser(userLoginManager.getCurrentUser());
        sides = new HashMap<Integer, DiceSide>();
        for(int i=1; i<=12; i++)
        {
            sides.put(i, null);
        }
    }

    /**
     * Saves current dice configuration.
     */
    public void confirmMapping()
    {

    }

    /**
     * Aborts the dice configuration
     */
    public void abortMapping()
    {

    }

    /**
     * Method used to inform the view of the current controller state
     * If 0 is returned, that means no dice is assigned to the user.
     * If 1 is returned, a dice is found for the user, but the dice is not in configuration mode.
     * If 2 is returned, the dice is currently in configuration mode.
     * @return An integer 0-2 according to the method description.
     */
    public int getStatus()
    {
        if(dice == null) return 0;
        if(inConfiguration) return 2;
        return 1;
    }

    public Dice getDice()
    {
        return dice;
    }

    public void setDice(Dice dice)
    {
        this.dice = dice;
    }

    public void startConfigureDice()
    {
        inConfiguration = true;
    }

    public Map<Integer, DiceSide> getSides() {
        return sides;
    }

    public void setSides(Map<Integer, DiceSide> sides) {
        this.sides = sides;
    }

    public Collection<BookingCategory> getAvailableCategories()
    {
        if(userLoginManager.getCurrentUser().getAssignedTeam() == null)
        {
            return bookingCategoryService.findAllCategories();
        }
        else
        {
            return bookingCategoryService.findAllCategoriesByTeam();
        }
    }
}
