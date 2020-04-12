package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.DiceRepository;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("application")
public class DiceService {

    private final DiceRepository diceRepository;
    private final ProdigaUserLoginManager prodigaUserLoginManager;

    public DiceService(DiceRepository diceRepository, ProdigaUserLoginManager prodigaUserLoginManager) {
        this.diceRepository = diceRepository;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
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
            if(dice.getAssignedRaspberry() == null) {
                throw new ProdigaGeneralExpectedException("Dice has to be assigned to a RaspberryPi", MessageType.WARNING);
            }

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
}
