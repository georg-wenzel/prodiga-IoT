package uibk.ac.at.prodiga.repositories;

import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;

import java.util.List;
import java.util.Optional;

public interface DiceRepository extends AbstractRepository<Dice, Long> {

    /**
     * Returns all dices which are  assigned to the given raspi
     * @param raspi The raspi to search for
     * @return A list with all dices
     */
    List<Dice> findAllByAssignedRaspberry(RaspberryPi raspi);

    /**
     * Returns the dice which is used by the given user
     * @param u The user to search for
     * @return The dice assigned to the given user
     */
    Dice findFirstByUser(User u);

    /**
     * Returns the dice with the given internal id
     * @param internalId The internal id
     * @return The found dice
     */
    Dice findFirstByInternalId(String internalId);
}
