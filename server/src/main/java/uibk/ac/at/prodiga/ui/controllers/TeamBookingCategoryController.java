package uibk.ac.at.prodiga.ui.controllers;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.services.BookingCategoryService;
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for teamwide configuration of possible booking categories
 */
@Component
@Scope("view")
public class TeamBookingCategoryController implements Serializable
{
    private static final long serialVersionUID = 5325687687418577315L;

    private Team team;
    private Collection<BookingCategory> categories;
    private Collection<BookingCategory> teamHasCategories;

    private Map<BookingCategory, Integer> usedInTeamBookings;
    private Map<BookingCategory, Integer> usedInDice;


    private final ProdigaUserLoginManager userLoginManager;
    private final BookingCategoryService bookingCategoryService;
    private final BookingService bookingService;
    private final DiceService diceService;

    public TeamBookingCategoryController(ProdigaUserLoginManager userLoginManager, BookingCategoryService bookingCategoryService, BookingService bookingService, DiceService diceService)
    {
        this.userLoginManager = userLoginManager;
        this.bookingCategoryService = bookingCategoryService;
        this.bookingService = bookingService;
        this.diceService = diceService;

        usedInTeamBookings = new HashMap<BookingCategory, Integer>();
        usedInDice = new HashMap<BookingCategory, Integer>();
    }

    /**
     * Returns whether or not the team is currently assigned this booking category
     * @param category The category to check
     * @return A boolean whether or not this booking category is in the team pool.
     */
    public boolean getCurrentlyUsedByTeam(BookingCategory category)
    {
        return getTeamHasCategories().contains(category);
    }

    //Needed for primefaces not to complain
    public void setGetCurrentlyUsedByTeam(boolean test)
    {
    }

    /**
     * Handles a change on a checkbox in the view, i.e category is allowed/disallowed for team.
     * @param category The category to handle
     * @throws ProdigaGeneralExpectedException Thrown saving the category state produces a warning or error.
     */
    public void categoryChanged(BookingCategory category) throws ProdigaGeneralExpectedException
    {
        if(getUsedInDice(category) > 0 && teamHasCategories.contains(category)) throw new ProdigaGeneralExpectedException("Attempt to remove category which is still used by dice.", MessageType.ERROR);

        if(teamHasCategories.contains(category))
        {
            teamHasCategories.remove(category);
            bookingCategoryService.disallowForTeam(category);
        }
        else
        {
            teamHasCategories.add(category);
            bookingCategoryService.allowForTeam(category);
        }
    }

    public Team getTeam()
    {
        if(this.team == null)
            this.team = userLoginManager.getCurrentUser().getAssignedTeam();

        return this.team;
    }

    public Collection<BookingCategory> getCategories()
    {
        if(categories == null)
            categories = bookingCategoryService.findAllCategories();
        return categories;
    }

    public Collection<BookingCategory> getTeamHasCategories()
    {
        if(teamHasCategories == null)
            teamHasCategories = getCategories().stream().filter(x -> x.getTeams().contains(getTeam())).collect(Collectors.toList());

        return teamHasCategories;
    }

    public int getUsedInBookingsByTeam(BookingCategory category)
    {
        if(!usedInTeamBookings.containsKey(category)) usedInTeamBookings.put(category, bookingService.getNumberOfTeamBookingsWithCategory(category));
        return usedInTeamBookings.get(category);
    }

    public int getUsedInDice(BookingCategory category)
    {
        if(!usedInDice.containsKey(category)) usedInDice.put(category, diceService.getDiceCountByCategoryAndTeam(category));
        return usedInDice.get(category);
    }

}
