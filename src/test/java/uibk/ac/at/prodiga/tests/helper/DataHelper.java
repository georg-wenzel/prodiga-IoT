package uibk.ac.at.prodiga.tests.helper;

import org.mockito.internal.util.collections.Sets;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.UserRepository;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DataHelper {

    /**
     * Creates an admin user with the given username
     * @param username User name
     * @param userRepository User repository to save user
     * @return The newly created user
     */
    public static User createAdminUser(String username, UserRepository userRepository) {
        return createUserWithRoles(username, Sets.newSet(UserRole.ADMIN), userRepository);
    }

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

}
