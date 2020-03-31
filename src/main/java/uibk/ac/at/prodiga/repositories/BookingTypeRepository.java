package uibk.ac.at.prodiga.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uibk.ac.at.prodiga.model.BookingType;

/**
 * DB Repository for managing Booking Types
 */
public interface BookingTypeRepository extends AbstractRepository<BookingType, Long>
{
    //Magic methods
    BookingType findFirstById(Long id);
    BookingType findAllActive();

    @Query("SELECT bt FROM BookingType bt WHERE bt.side = :side " +
            "AND bt.isActive = true")
    BookingType findActiveCategoryForSide(@Param("side") int side);
}
