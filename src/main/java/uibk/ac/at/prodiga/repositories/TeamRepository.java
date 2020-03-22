package uibk.ac.at.prodiga.repositories;

import uibk.ac.at.prodiga.model.Team;

/**
 * DB repository for managing teams
 */
public interface TeamRepository extends AbstractRepository<Team, Long>
{
    //Magic methods
    Team findFirstById(Long id);
    Team findFirstByName(String name);
}
