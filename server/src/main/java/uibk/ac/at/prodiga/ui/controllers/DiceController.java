package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.services.UserService;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;
import uibk.ac.at.prodiga.utils.SnackbarHelper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Component
@Scope("view")
public class DiceController implements Serializable {

    private static final long serialVersionUID = 5325687687622577315L;

    private final DiceService diceService;
    private final UserService userService;
    private final ProdigaUserLoginManager prodigaUserLoginManager;
    private Dice dice;
    private Collection<Dice> dices;

    public DiceController(DiceService diceService, ProdigaUserLoginManager prodigaUserLoginManager, UserService userService) {
        this.diceService = diceService;
        this.userService = userService;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
    }


    public Dice getDice()
    {
        return this.dice;
    }

    public String getDiceUser()
    {
        if(this.dice == null || this.dice.getUser() == null) return "";
        return this.dice.getUser().getUsername();
    }

    public void setDiceUser(String user)
    {
        if(user == null || user.isEmpty()) this.dice.setUser(null);
        this.dice.setUser(userService.loadUser(user));
    }

    public void setDice(Dice dice){
        this.dice = dice;
    }

    /**
     * Returns all dices
     * @return A list with dices
     */
    public Collection<Dice> getAllDices() {
        if(dices == null) {
            dices = this.diceService.getAllDice();
        }
        return dices;
    }

    /**
     * Saves currently selected dice
     * @throws Exception when save fails
     */
    public void doSaveDice() throws Exception {
        //fix user if null selected
        diceService.save(dice);
        SnackbarHelper.getInstance().showSnackBar("Dice " + dice.getInternalId() + " saved!", MessageType.INFO);
    }

    /**
     * Returns all pending dices
     * @return A list with dice entities
     */
    public List<Dice> getAllPendingDices() {
        return diceService.getPendingDices();
    }

    /**
     * pls ignore me
     */
    public void setAllPendingDices(List<Dice> dices) {
        // Needed because JSF lol
    }

    /**
     * Saves the given dice
     * @param d The dice
     */
    public void savePendingDice(Dice d) throws ProdigaGeneralExpectedException {
        Dice result = diceService.save(d);
        SnackbarHelper.getInstance().showSnackBar("Dice " + result.getInternalId() + " added!", MessageType.INFO);
    }

    /**
     * Gets dice by id.
     *
     * @return the dice by id
     */
    public Long getDiceById() {
        if(this.dice == null){
            return null;
        }
        return this.dice.getId();
    }

    /**
     * Sets current dice by diceId
     * @throws Exception when dice could not be found
     */
    public void setDiceById(Long diceId) throws Exception{
        loadDiceById(diceId);
    }

    /**
     * Sets currently active dice by the id
     * @param diceId when diceId could not be found
     */
    public void loadDiceById(Long diceId) throws ProdigaGeneralExpectedException {
        if (diceId != null) {
            this.dice = diceService.loadDice(diceId);
        } else {
            this.dice = diceService.createDice();
            this.dice.setUser(new User());
            this.dice.setAssignedRaspberry(new RaspberryPi());
        }
    }

    /**
     * Deletes the dices.
     *
     */
    public void deleteDice(Dice dice) throws Exception {
        this.diceService.deleteDice(dice);
        dices = null;
        SnackbarHelper.getInstance()
                .showSnackBar("Dice \"" + dice.getInternalId() + "\" deleted!", MessageType.ERROR);
    }

    /**
     * Returns the battery info for the current user
     * @return Battery info
     */
    public String getDiceBatteryInfoForCurrentUser() {
        return diceService.getDiceBatteryStatusForUser(prodigaUserLoginManager.getCurrentUser());
    }
}
