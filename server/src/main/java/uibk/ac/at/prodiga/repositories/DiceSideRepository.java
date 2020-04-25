package uibk.ac.at.prodiga.repositories;

import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.DiceSide;
import uibk.ac.at.prodiga.services.DiceService;

import java.util.Collection;

public interface DiceSideRepository extends AbstractRepository<DiceSide, Long> {

    DiceSide findFirstByDiceAndSide(Dice d, int side);
    Collection<DiceSide> findAllByDice(Dice d);
}
