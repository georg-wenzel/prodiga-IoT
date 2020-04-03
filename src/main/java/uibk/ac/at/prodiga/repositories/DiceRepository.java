package uibk.ac.at.prodiga.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;

import java.util.List;

public interface DiceRepository extends AbstractRepository<Dice, Long>
{

    Dice findFirstById(long id);

    /**
     * Returns alll dices which are  aassigned to the given raspi
     * @param raspi The raspi to search for
     * @return A list with all dices
     */
    List<Dice> findAllByAssignedRaspberry(RaspberryPi raspi);

    @Query("SELECT d FROM Dice d WHERE d.user = :user")
    Dice findDiceByUser(@Param("user") User user);

}
