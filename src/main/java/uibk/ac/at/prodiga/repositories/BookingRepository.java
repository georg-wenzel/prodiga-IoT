package uibk.ac.at.prodiga.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.Dice;

/**
 * DB Repository for managing Booking Types
 */
public interface BookingRepository extends AbstractRepository<Booking, Long>
{
    //Magic methods
    Booking findFirstById(Long id);
    Booking findAllByDice(Dice d);
}