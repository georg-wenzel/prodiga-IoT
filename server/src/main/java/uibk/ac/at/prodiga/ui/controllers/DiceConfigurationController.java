package uibk.ac.at.prodiga.ui.controllers;

import org.javatuples.Pair;
import org.primefaces.PrimeFaces;
import org.primefaces.expression.ComponentNotFoundException;
import org.primefaces.expression.SearchExpressionFacade;
import org.primefaces.util.LangUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.services.BookingCategoryService;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.services.DiceSideService;
import uibk.ac.at.prodiga.utils.*;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.security.KeyPair;
import java.util.*;
import java.util.logging.Level;

/**
 * Controller for managing user dice configuration
 */
@Component
@Scope("view")
public class DiceConfigurationController implements Serializable
{
    private static final long serialVersionUID = 5125687687692577315L;

    private final ProdigaUserLoginManager userLoginManager;
    private final DiceService diceService;
    private final DiceSideService diceSideService;
    private final BookingCategoryService bookingCategoryService;

    private Dice dice;
    private UUID uuid;
    private int currentSide = -1;
    private boolean sideChanged = false;
    private boolean inConfiguration = false;
    private boolean inEditing = false;
    private DiceConfigurationWrapper wrapper;

    private Map<Integer, BookingCategory> sides;
    private Map<Integer, String> newSides;

    public DiceConfigurationController(ProdigaUserLoginManager userLoginManager, DiceService diceService, DiceSideService diceSideService, BookingCategoryService bookingCategoryService)
    {
        this.userLoginManager = userLoginManager;
        this.diceService = diceService;
        this.diceSideService = diceSideService;
        this.bookingCategoryService = bookingCategoryService;
        this.dice = this.diceService.getDiceByUser(userLoginManager.getCurrentUser());

        sides = new HashMap<Integer, BookingCategory>();
        newSides = new HashMap<Integer, String>();
    }

    private void prepareSideCollections()
    {
        Collection<DiceSide> currSides = diceSideService.findByDice(dice);
        for (int i = 1; i <= 12; i++)
        {
            //because lambda expression should be final..
            int finalI = i;

            Optional<DiceSide> side = currSides.stream().filter(x -> x.getSideFriendlyName() == finalI).findFirst();
            if (side.isPresent()) {
                sides.put(i, side.get().getBookingCategory());
                newSides.put(i, side.get().getBookingCategory().getId().toString());
            }
            else {
                sides.put(i, null);
                newSides.put(i, null);
            }
        }
    }

    private void prepareEmptySideCollections()
    {
        Collection<DiceSide> currSides = diceSideService.findByDice(dice);
        for (int i = 1; i <= 12; i++)
        {
            //because lambda expression should be final..
            int finalI = i;

            Optional<DiceSide> side = currSides.stream().filter(x -> x.getSideFriendlyName() == finalI).findFirst();
            if (side.isPresent()) {
                sides.put(i, side.get().getBookingCategory());
            }
            else {
                sides.put(i, null);
            }

            newSides.put(i, null);
        }
    }

    /**
     * Saves current dice configuration.
     */
    public void confirmMapping() throws ProdigaGeneralExpectedException
    {
        for(Map.Entry<Integer, String> kvp: newSides.entrySet())
        {
            if(kvp.getValue() == null) continue;
            wrapper.getCompletedSides().put(kvp.getKey(), new Pair<>(kvp.getKey(), bookingCategoryService.findById(Long.parseLong(kvp.getValue()))));
        }
        diceService.completeConfiguration(dice);
        abortMapping();
        SnackbarHelper.getInstance().showSnackBar("Mapping saved", MessageType.INFO);
    }

    /**
     * Saves current editing of dice
     */
    public void confirmEdit() throws ProdigaGeneralExpectedException
    {
        if(newSides.entrySet().stream().noneMatch(x -> x.getValue() != null && bookingCategoryService.findById(Long.parseLong(x.getValue())).getId().equals(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID)))
            throw new ProdigaGeneralExpectedException("At least one side must be configured with " + bookingCategoryService.findById(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID).getName(), MessageType.ERROR);

        for(Map.Entry<Integer, String> kvp: newSides.entrySet())
        {
            DiceSide side = diceSideService.findByDiceAndFriendlySide(dice, kvp.getKey());
            if(side != null)
            {
                if(kvp.getValue() == null) side.setBookingCategory(null);
                else side.setBookingCategory(bookingCategoryService.findById(Long.parseLong(kvp.getValue())));
                diceSideService.save(side);
            }
        }
        abortEdit();
        SnackbarHelper.getInstance().showSnackBar("Mapping saved", MessageType.INFO);
    }

    /**
     * Aborts the dice configuration
     */
    public void abortMapping()
    {
        diceService.unregisterNewSideCallback(uuid, dice.getInternalId());
        inConfiguration = false;
    }

    /**
     * Aborts the editing
     */
    public void abortEdit()
    {
        inEditing = false;
    }

    /**
     * Method used to inform the view of the current controller state
     * If 0 is returned, that means no dice is assigned to the user.
     * If 1 is returned, a dice is found for the user, but the dice is not in configuration mode.
     * If 2 is returned, the dice is currently in configuration mode.
     * If 3 is returned, the dice is currently in editing mode.
     * @return An integer 0-2 according to the method description.
     */
    public int getStatus()
    {
        if(dice == null) return 0;
        if(inConfiguration) return 2;
        if(inEditing) return 3;
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

    public void startConfigureDice() throws ProdigaGeneralExpectedException
    {
        if(dice == null) throw new ProdigaGeneralExpectedException("Dice Configuration cannot be started because no dice was found for the user.", MessageType.ERROR);

        //Force an update of current dice categories
        prepareEmptySideCollections();

        uuid = UUID.randomUUID();
        wrapper = diceService.addDiceToConfiguration(dice);
        currentSide = wrapper.getCurrentSide();
        diceService.registerNewSideCallback(uuid, x -> {
            if(x.getValue0().compareTo(uuid) != 0) return;

            //Confirms to the dice service that the callback is still being listened on.
            diceService.ensureListening(uuid, x.getValue1().getDice().getInternalId());

            //change nothing if the side is the same as the existing side.
            if(x.getValue1().getCurrentSideFriendlyName() == currentSide) return;

            //Change side and request form update.
            currentSide = x.getValue1().getCurrentSideFriendlyName();
            sideChanged = true;
        });
        inConfiguration = true;
    }

    public void startEditDice() throws ProdigaGeneralExpectedException
    {
        if(dice == null) throw new ProdigaGeneralExpectedException("Dice editing cannot be started because no dice was found for the user.", MessageType.ERROR);

        //Force an update of current dice categories
        prepareSideCollections();

        inEditing = true;
    }

    /**
     * Poll method is called every 2 seconds by the active view.
     * If the side has changed since the last poll, force an update of the table via ajax.
     */
    public void pollUpdate()
    {
        if(sideChanged)
        {
            PrimeFaces.current().ajax().update(":diceConfigForm");
            sideChanged = false;
        }
    }

    public Map<Integer, BookingCategory> getSides() {
        return sides;
    }

    public void setSides(Map<Integer, BookingCategory> sides) {
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

    public int getCurrentSide() {
        return currentSide;
    }

    public BookingCategory getSide(int key)
    {
        return sides.get(key);
    }

    public Map<Integer, String> getNewSides() {
        return newSides;
    }

    public void setNewSides(Map<Integer, String> newSides) {
        this.newSides = newSides;
    }
}
