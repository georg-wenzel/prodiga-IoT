package uibk.ac.at.prodiga.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;

public interface UserRepository extends AbstractRepository<User, String> {

    User findFirstByUsername(String username);

    List<User> findAllByAssignedTeam(Team team);

    List<User> findByUsernameContaining(String username);

    @Query("SELECT u FROM User u WHERE CONCAT(u.firstName, ' ', u.lastName) = :wholeName")
    List<User> findByWholeNameConcat(@Param("wholeName") String wholeName);

    @Query("SELECT u FROM User u WHERE :role MEMBER OF u.roles")
    List<User> findByRole(@Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE 'TEAMLEADER' MEMBER OF u.roles " +
            "AND u.assignedTeam = :team")
    User findTeamLeaderOf(@Param("team") Team team);

    @Query("SELECT u FROM User u WHERE 'DEPARTMENTLEADER' MEMBER OF u.roles " +
            "AND u.assignedDepartment = :department")
    User findDepartmentLeaderOf(@Param("department") Department department);

    /**
     * Returns the first user with the given email
     * @param email The email
     * @return An optional containing the user
     */
    Optional<User> findFirstByEmail(String email);
}
