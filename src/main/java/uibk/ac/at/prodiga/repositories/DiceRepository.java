package uibk.ac.at.prodiga.repositories;

import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.RaspberryPi;

import java.util.List;

public interface DiceRepository extends AbstractRepository<Dice, Long> {

    /**
     * Returns alll dices which are  aassigned to the given raspi
     * @param raspi The raspi to search for
     * @return A list with all dices
     */
    List<Dice> findAllByAssignedRaspberry(RaspberryPi raspi);

}
