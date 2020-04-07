package uibk.ac.at.prodiga.repositories;

import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.Vacation;

import java.util.Collection;

public interface VacationRepository extends AbstractRepository<Vacation, Long>
{
    //Magic methods
    Vacation findFirstById(Long id);
    Collection<Vacation> findAllByUser(User user);
}
