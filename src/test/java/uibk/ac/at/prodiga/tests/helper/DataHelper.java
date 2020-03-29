package uibk.ac.at.prodiga.tests.helper;

import org.mockito.internal.util.collections.Sets;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.DiceRepository;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
import uibk.ac.at.prodiga.repositories.RoomRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.utils.Constants;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DataHelper {

    public static String TEST_PASSWORD = "passwd";
    public static String TEST_PASSWORD_ENCODED = "$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC";

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
        u.setPassword(TEST_PASSWORD_ENCODED);
        u.setUpdateDate(new Date());

        return userRepository.save(u);
    }

    /**
     * Creates a given dice with the given data
     * @param internalId The internal Id used by the dice (and the raspi if not exists)
     * @param raspi The raspi may be null
     * @param u The user which creates all objects
     * @param diceRepository The Repository to save the dice
     * @param raspberryPiRepository The Repository to save the raspi
     * @param roomRepository The Repository to save the room
     * @return The newly created Dice
     */
    public static Dice createDice(String internalId,
                                  RaspberryPi raspi,
                                  User u,
                                  DiceRepository diceRepository,
                                  RaspberryPiRepository raspberryPiRepository,
                                  RoomRepository roomRepository) {
        if(raspi == null) {
            raspi = createRaspi(internalId, u, null, raspberryPiRepository, roomRepository);
        }

        Dice d = new Dice();
        d.setAssignedRaspberry(raspi);
        d.setInternalId(internalId);
        d.setObjectChangedDateTime(new Date());
        d.setObjectChangedUser(u);
        d.setObjectCreatedDateTime(new Date());
        d.setObjectCreatedUser(u);

        return diceRepository.save(d);
    }

    /**
     * Creates a raspi with the given internal Id
     * @param internalId The internal ID to use
     * @param raspberryPiRepository The repository to save the raspi
     * @param u User which created the raspi
     * @param r Room in which the raspi gets saved can be null
     * @return The saved raspi
     */
    public static RaspberryPi createRaspi(String internalId,
                                          User u,
                                          Room r,
                                          RaspberryPiRepository raspberryPiRepository,
                                          RoomRepository roomRepository) {
        if(r == null) {
            r = createRoom(createRandomString(20), u, roomRepository);
        }

        RaspberryPi raspi = new RaspberryPi();
        raspi.setInternalId(internalId);
        raspi.setPassword(TEST_PASSWORD_ENCODED);
        raspi.setObjectChangedDateTime(new Date());
        raspi.setObjectCreatedDateTime(new Date());
        raspi.setObjectChangedUser(u);
        raspi.setObjectCreatedUser(u);
        raspi.setAssignedRoom(r);

        return raspberryPiRepository.save(raspi);
    }

    /**
     * Creates a new room with the given user and the given name
     * @param name Rooms name
     * @param u  user which created the room
     * @param roomRepository Repository used to save the room
     * @return The newly created room
     */
    public static Room createRoom(String name, User u, RoomRepository roomRepository) {
        Room r = new Room();
        r.setName(name);
        r.setObjectChangedDateTime(new Date());
        r.setObjectChangedUser(u);
        r.setObjectCreatedDateTime(new Date());
        r.setObjectCreatedUser(u);

        return roomRepository.save(r);
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
