package uibk.ac.at.prodiga.tests.helper;

import org.mockito.internal.util.collections.Sets;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.DepartmentRepository;
import uibk.ac.at.prodiga.repositories.TeamRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DataHelper {

    /**
     * Creates a new random user with random username
     * @return The newly created username
     */
    public static User createRandomUser(UserRepository userRepository) {
        String username = createRandomString(30);

        return createUserWithRoles(username, Sets.newSet(UserRole.EMPLOYEE), userRepository);
    }

    /**
     * Creates an admin user with the given username
     * @param username User name
     * @param userRepository User repository to save user
     * @return The newly created user
     */
    public static User createAdminUser(String username, UserRepository userRepository) {
        return createUserWithRoles(username, Sets.newSet(UserRole.ADMIN), userRepository);
    }

    /**
     * Creates a user with the given user name and roles
     * @param username The username to use
     * @param roles The roles to use
     * @param userRepository The repository to use
     * @return The newly created user
     */
    public static User createUserWithRoles(String username, Set<UserRole> roles, UserRepository userRepository) {
        User u = new User();
        u.setUsername(username);
        u.setCreateDate(new Date());
        u.setRoles(roles);
        u.setCreateUser(null);
        u.setEmail("test@test.com");
        u.setId(username);
        u.setEnabled(true);
        u.setFirstName("Generic");
        u.setLastName("Namer");
        u.setPassword("$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC");
        u.setUpdateDate(new Date());

        return userRepository.save(u);
    }

    /**
     * Creates a user with a random user name and roles, as well as a certain department and team
     * @param roles The roles to use
     * @param createUser the creation user for this user.
     * @param dept The department the user is in
     * @param team The team the user is in
     * @param userRepository The repository to use
     * @return The newly created user
     */
    public static User createUserWithRoles(Set<UserRole> roles, User createUser, Department dept, Team team, UserRepository userRepository)
    {
        User u = new User();
        u.setUsername(createRandomString(30));
        u.setCreateDate(new Date());
        u.setRoles(roles);
        u.setCreateUser(createUser);
        u.setEmail("test@test.com");
        u.setId(createRandomString(30));
        u.setEnabled(true);
        u.setAssignedDepartment(dept);
        u.setAssignedTeam(team);
        u.setFirstName("Generic");
        u.setLastName("Namer");
        u.setPassword("$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC");
        u.setUpdateDate(new Date());

        return userRepository.save(u);
    }

    public static Department createRandomDepartment(User createUser, DepartmentRepository departmentRepository)
    {
        String name = createRandomString(30);

        Department dept = new Department();
        dept.setName(name);
        dept.setObjectCreatedUser(createUser);
        dept.setObjectCreatedDateTime(new Date());
        return departmentRepository.save(dept);
    }

    public static Team createRandomTeam(Department dept,  User createUser, TeamRepository teamRepository)
    {
        String name = createRandomString(30);

        Team team = new Team();
        team.setName(name);
        team.setObjectCreatedUser(createUser);
        team.setObjectCreatedDateTime(new Date());
        team.setDepartment(dept);

        return teamRepository.save(team);
    }

    private static String createRandomString(int size) {
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";


        StringBuilder sb = new StringBuilder(size);

        for (int i = 0; i < size; i++) {
            int index = (int)(alphaNumericString.length() * Math.random());

            sb.append(alphaNumericString.charAt(index));
        }

        return sb.toString();
    }

}
