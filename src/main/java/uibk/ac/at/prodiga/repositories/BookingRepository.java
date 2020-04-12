package uibk.ac.at.prodiga.repositories;
import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.Team;

import java.util.Collection;

/**
 * DB Repository for managing Booking Types
 */
public interface BookingRepository extends AbstractRepository<Booking, Long>
{
    //Magic methods
    Booking findFirstById(Long id);
    Collection<Booking> findAllByDice(Dice d);
    Collection<Booking> findAllByTeam(Team team);
    Collection<Booking> findAllByDept(Department department);
}