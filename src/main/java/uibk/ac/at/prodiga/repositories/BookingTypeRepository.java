package uibk.ac.at.prodiga.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uibk.ac.at.prodiga.model.BookingType;

import java.util.Collection;

/**
 * DB Repository for managing Booking Types
 */
public interface BookingTypeRepository extends AbstractRepository<BookingType, Long>
{
    //Magic methods
    BookingType findFirstById(Long id);;

    @Query("SELECT bt FROM BookingType bt WHERE bt.side = :side " +
            "AND bt.isActive = true")
    BookingType findActiveCategoryForSide(@Param("side") int side);

    @Query("SELECT bt FROM BookingType bt WHERE bt.isActive = true")
    Collection<BookingType> findAllActiveCategories();
}
