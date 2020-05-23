package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.services.DiceService;
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
    private final ProdigaUserLoginManager prodigaUserLoginManager;
    private Dice dice;
    private Collection<Dice> dices;

    public DiceController(DiceService diceService, ProdigaUserLoginManager prodigaUserLoginManager) {
        this.diceService = diceService;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
    }


    public Dice getDice(){
        return this.dice;
    }

    public void setDice(Dice dice){
        this.dice = dice;
    }

    /**
     * Returns all dices
     * @return A list with dices
     */
    public Collection<Dice> getAllDices() {
       if(dices == null) dices = this.diceService.getAllDice();
        return dices;
    }

    /**
     * Gets the dice by the given id
     * @param diceId The dice id
     * @return The found dive
     * @throws ProdigaGeneralExpectedException If no dice could be found
     */
    public Dice loadDice(long diceId) throws ProdigaGeneralExpectedException {
        return this.diceService.loadDice(diceId);
    }

    /**
     * Returns the dice with the given internal id
     * @param internalId the internal id
     * @return The found dice
     */
    public Dice getDiceByInternalId(String internalId) {
        return this.diceService.getDiceByInternalIdWithAuth(internalId);
    }

    /**
     * Gets the dice assigned to the given user
     * @param u The user
     * @return The assigned dice
     */
    public Dice getDiceByUser(User u) {
        return this.getDiceByUser(u);
    }
    /**
     * Returns all dice which are a signed to teh given raspi
     * @param raspi The raspi
     * @return A list with dices
     */
    public List<Dice> getAllByRaspberryPi(RaspberryPi raspi) {
       return this.diceService.getAllByRaspberryPi(raspi);
    }

    /**
     * Returns all dices which are active and assigned to a user
     * @return A list of dices
     */
    public List<Dice> getAllAvailableDices() {
        return this.diceService.getAllAvailableDices();
    }

    /**
     * Saves currently selected dice
     * @throws Exception when save fails
     */
    public void doSaveDice() throws Exception {
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
            if(dice.getUser() == null) {
                dice.setUser(prodigaUserLoginManager.getCurrentUser());
            }
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
