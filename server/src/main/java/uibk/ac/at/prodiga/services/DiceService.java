package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.javatuples.Pair;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.DiceSide;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.DiceRepository;
import uibk.ac.at.prodiga.utils.DiceConfigurationWrapper;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@Scope("application")
public class DiceService {

    private final DiceRepository diceRepository;
    private final ProdigaUserLoginManager prodigaUserLoginManager;
    private final LogInformationService logInformationService;
    private final DiceSideService diceSideService;

    private final Map<String, DiceConfigurationWrapper> diceConfigurationWrapperDict = new HashMap<>();
    private final Map<UUID, Consumer<Pair<UUID, DiceConfigurationWrapper>>> onNewDiceSideCallBackDict = new HashMap<>();

    public DiceService(DiceRepository diceRepository, ProdigaUserLoginManager prodigaUserLoginManager, DiceSideService diceSideService, LogInformationService logInformationService) {
        this.diceRepository = diceRepository;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
        this.diceSideService = diceSideService;
        this.logInformationService = logInformationService;
    }

    /**
     * Returns all dices
     * @return A list with dices
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Dice> getAllDice() {
        return Lists.newArrayList(diceRepository.findAll());
    }

    /**
     * Gets the dice by the given id
     * @param diceId The dice id
     * @return The found dive
     * @throws ProdigaGeneralExpectedException If no dice could be found
     */
    public Dice loadDice(long diceId) throws ProdigaGeneralExpectedException {
        return diceRepository.findById(diceId).orElseThrow(()
                -> new ProdigaGeneralExpectedException("No dice with Id " + diceId + " found",
                MessageType.WARNING));
    }

    /**
     * Returns the dice with the given internal id
     * @param internalId the internal id
     * @return The found dice
     */
    public Dice getDiceByInternalId(String internalId) {
        return diceRepository.findFirstByInternalId(internalId);
    }

    /**
     * Gets the dice assigned to the given user
     * @param u The user
     * @return The assigned dice
     */
    public Dice getDiceByUser(User u) {
        if(u == null) {
            return null;
        }

        return diceRepository.findFirstByUser(u);
    }

    /**
     * Returns all dice which are a signed to teh given raspi
     * @param raspi The raspi
     * @return A list with dices
     */
    public List<Dice> getAllByRaspberryPi(RaspberryPi raspi) {
        return diceRepository.findAllByAssignedRaspberry(raspi);
    }

    /**
     * Returns all dices which are active and assigned to a user
     * @return A list of dices
     */
    public List<Dice> getAllAvailableDices() {
        return getAllDice().stream()
                .filter(x -> x.isActive() && x.getUser() == null)
                .collect(Collectors.toList());
    }

    /**
     * Saves the given dice
     * @param dice The dice to save
     * @return The saved dice
     * @throws ProdigaGeneralExpectedException Either the dice does'nt have a assigned raspi, user or internalId
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Dice save(Dice dice) throws ProdigaGeneralExpectedException {
        if(dice.isActive()) {

            if(StringUtils.isEmpty(dice.getInternalId())) {
                throw new ProdigaGeneralExpectedException("Dice needs a internal id", MessageType.WARNING);
            }

            // Here we check if the user already has a dice
            if(dice.getUser() != null) {
                Dice userDice = getDiceByUser(dice.getUser());
                // If the user already has a dice and the current dice is new we throw
                // ||
                // If the user already has a dice and the current dice is NOT the usersDice we also throw
                if(userDice != null && (dice.isNew() || !dice.getId().equals(userDice.getId()))) {
                    throw new ProdigaGeneralExpectedException("User " + dice.getUser().getUsername()
                            + " already has a assigned Dice", MessageType.WARNING);
                }
            }
        }

        if(dice.isNew()) {
            dice.setObjectCreatedDateTime(new Date());
            dice.setObjectCreatedUser(prodigaUserLoginManager.getCurrentUser());
        }

        dice.setObjectChangedUser(prodigaUserLoginManager.getCurrentUser());
        dice.setObjectChangedDateTime(new Date());

        return diceRepository.save(dice);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Dice createDice()
    {
        return new Dice();
    }

    /**
     * Deletes the dice.
     *
     * @param dice the dice to delete
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteDice(Dice dice) {
        diceRepository.delete(dice);
        logInformationService.log("Dice " + dice.getInternalId() + " was deleted!");
    }

    /**
     * If the dice is in configuration mode sets the current side to the given side
     * and notifies all subscribers about the change
     * @param internalId The dices internalId
     * @param side The current Side of the dice
     */
    public void onNewDiceSide(String internalId, int side) {
        //First check if the dice is in config mode
        if(diceConfigurationWrapperDict.containsKey(internalId)) {
            DiceConfigurationWrapper wrapper = diceConfigurationWrapperDict.get(internalId);
            wrapper.setCurrentSide(side);

            onNewDiceSideCallBackDict.forEach((key, value) -> value.accept(Pair.with(key, wrapper)));
        }
    }

    /**
     * Returns whether the dice with given id is in configuration mode
     * @param internalId The dices internal id
     * @return Whether the dice is in configuration mode
     */
    public boolean diceInConfigurationMode(String internalId) {
        return diceConfigurationWrapperDict.containsKey(internalId);
    }

    /**
     * Can be called if your service wants to get notified when the dice side changes
     * @param id An identifier of your service
     * @param action The action which will be executed
     */
    public void registerNewSideCallback(UUID id, Consumer<Pair<UUID, DiceConfigurationWrapper>> action) {
        if(id == null) {
            return;
        }

        onNewDiceSideCallBackDict.put(id, action);
    }

    /**
     * Unregisters the service with the given id from the newSide Callback
     * @param id The services id
     */
    public void unregisterNewSideCallback(UUID id) {
        if(id == null) {
            return;
        }

        onNewDiceSideCallBackDict.remove(id);
    }

    /**
     * Adds the given dice in configuration mode
     * @param d The dice to add
     */
    public DiceConfigurationWrapper addDiceToConfiguration(Dice d) {
        if(d == null) {
            return null;
        }

        DiceConfigurationWrapper wrapper = new DiceConfigurationWrapper();
        wrapper.setCurrentSide(-1);
        wrapper.setDice(d);

        diceConfigurationWrapperDict.put(d.getInternalId(), wrapper);

        return wrapper;
    }

    /**
     * Completes the given Configuration for the given dice
     * @param d The dice
     * @throws ProdigaGeneralExpectedException When dice == null or not enough sides are configured
     */
    public void completeConfiguration(Dice d) throws ProdigaGeneralExpectedException {
        if(d == null) {
            return;
        }

        DiceConfigurationWrapper wrapper = diceConfigurationWrapperDict.getOrDefault(d.getInternalId(), null);

        if(wrapper == null) {
            return;
        }

        if(wrapper.getDice() == null) {
            throw new ProdigaGeneralExpectedException("Cannot complete configuration without dice", MessageType.ERROR);
        }

        if(wrapper.getCompletedSides() == null) {
            throw new ProdigaGeneralExpectedException("Cannot complete configuration without completed sides", MessageType.ERROR);
        }

        if(wrapper.getCompletedSides().size() != 12) {
            throw new ProdigaGeneralExpectedException("Exactly 12 sides need to be configured", MessageType.ERROR);
        }

        wrapper.getCompletedSides().forEach((key, value) -> {
            DiceSide ds = new DiceSide();
            ds.setBookingCategory(value);
            ds.setDice(wrapper.getDice());
            ds.setSide(key);
            diceSideService.saveOrModify(ds);
        });

        diceConfigurationWrapperDict.remove(wrapper.getDice().getInternalId());
    }
}
