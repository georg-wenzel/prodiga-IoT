package uibk.ac.at.prodiga.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;

import java.util.Collection;
import java.util.Date;

public interface BookingCategoryRepository extends AbstractRepository<BookingCategory, Long>
{
    @Query("SELECT bc FROM BookingCategory bc WHERE NOT bc.id = :exceptId")
    Collection<BookingCategory> findAllExcept(Long exceptId);
    @Query("SELECT bc FROM BookingCategory bc WHERE NOT bc.id = :exceptId AND :t MEMBER OF bc.teams")
    Collection<BookingCategory> findAllByTeamExcept(Team t, Long exceptId);
    @Query("SELECT bc FROM BookingCategory bc WHERE NOT bc.id = :exceptId AND NOT :t MEMBER OF bc.teams")
    Collection<BookingCategory> findAllWithoutTeamExcept(Team t, Long exceptId);
}
