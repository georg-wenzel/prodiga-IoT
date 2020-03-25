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
     * Creates a new random user with random username
     * @return The newly created username
     */
    public static User createRandomUser(UserRepository userRepository) {
        String username = createRandomString(30);

        return createUserWithRoles(username, null, userRepository);
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
