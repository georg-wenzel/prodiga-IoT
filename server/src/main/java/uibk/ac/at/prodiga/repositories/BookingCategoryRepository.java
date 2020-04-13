package uibk.ac.at.prodiga.repositories;


import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Team;

import java.util.Collection;

public interface BookingCategoryRepository extends AbstractRepository<BookingCategory, Long> {

    Collection<BookingCategory> findAllByTeamsContaining(Team t);

    Collection<BookingCategory> findAllByTeamsNotContaining(Team t);
}
