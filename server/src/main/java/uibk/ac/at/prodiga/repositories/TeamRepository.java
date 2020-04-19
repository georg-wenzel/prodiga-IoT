package uibk.ac.at.prodiga.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;

import java.util.List;

/**
 * DB repository for managing teams
 */
public interface TeamRepository extends AbstractRepository<Team, Long>
{
    //Magic methods
    Team findFirstById(Long id);
    Team findFirstByName(String name);

    @Query("SELECT t FROM Team t WHERE t.department = :department")
    List<Team> findTeamOfDepartment(@Param("department") Department department);
}
