package uibk.ac.at.prodiga.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.Team;

/**
 * DB Repository for managing Booking Types
 */
public interface BookingRepository extends AbstractRepository<Booking, Long>
{
    //Magic methods
    Booking findFirstById(Long id);
    Booking findAllByDice(Dice d);

    //TODO: Data inconsistency: What happens when user changes team/department? Maybe add flag in Booking itself?
    @Query("SELECT b FROM Booking b WHERE b.dice in " +
            "(" +
            "SELECT d from Dice d WHERE d.user.assignedTeam = :team" +
            ")")
    Booking findAllByTeam(@Param("team") Team team);

    @Query("SELECT b FROM Booking b WHERE b.dice in " +
            "(" +
            "SELECT d from Dice d WHERE d.user.assignedDepartment = :department" +
            ")")
    Booking findAllByDepartment(@Param("department") Department department);
}