package uibk.ac.at.prodiga.tests.helper;

import org.mockito.internal.util.collections.Sets;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.*;
import uibk.ac.at.prodiga.model.*;

import java.util.Date;
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
        u.setPassword(TEST_PASSWORD_ENCODED);
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
        u.setPassword(TEST_PASSWORD_ENCODED);
        u.setUpdateDate(new Date());

        return userRepository.save(u);
    }

    /**
     * Creates a random department
     * @param createUser The creation user of the department
     * @param departmentRepository The repository to save the department
     * @return The randomly generated department.
     */
    public static Department createRandomDepartment(User createUser, DepartmentRepository departmentRepository)
    {
        String name = createRandomString(30);

        Department dept = new Department();
        dept.setName(name);
        dept.setObjectCreatedUser(createUser);
        dept.setObjectCreatedDateTime(new Date());
        return departmentRepository.save(dept);
    }

    /**
     * Creates a random team within a department
     * @param dept The department the team is in
     * @param createUser The creation user for the team
     * @param teamRepository The repository to save the team.
     * @return The ranomly generated team.
     */
    public static Team createRandomTeam(Department dept,  User createUser, TeamRepository teamRepository) {
        String name = createRandomString(30);

        Team team = new Team();
        team.setName(name);
        team.setObjectCreatedUser(createUser);
        team.setObjectCreatedDateTime(new Date());
        team.setDepartment(dept);

        return teamRepository.save(team);
    }

    /**
     * Creates a booking type with random name and specified properties
     * @param side The side this booking type is for
     * @param active Whether or not the booking type is active
     * @param createUser The creation user for this booking type
     * @param bookingTypeRepository The repository to save the booking type.
     * @return The generated booking type.
     */
    public static BookingType createBookingType(int side, boolean active, User createUser, BookingTypeRepository bookingTypeRepository)
    {
        return createBookingType(side, active, createRandomString(20), createUser, bookingTypeRepository);
    }

    /**
     * Creates a booking type with specified properties
     * @param side The side this booking type is for
     * @param active Whether or not the booking type is active
     * @param activityName the name of the activity
     * @param createUser The creation user for this booking type
     * @param bookingTypeRepository The repository to save the booking type.
     * @return The generated booking type.
     */
    public static BookingType createBookingType(int side, boolean active, String activityName, User createUser, BookingTypeRepository bookingTypeRepository)
    {
        //if flag is active and active booking type for this side already returns, return that one (avoids conflicts when test data exists)
        if(active && bookingTypeRepository.findActiveCategoryForSide(side)  != null) return bookingTypeRepository.findActiveCategoryForSide(side);

        BookingType bt = new BookingType();
        bt.setActive(active);
        bt.setSide(side);
        bt.setActivityName(activityName);
        bt.setObjectCreatedUser(createUser);
        bt.setObjectCreatedDateTime(new Date());

        return bookingTypeRepository.save(bt);
    }

     /* Creates a given dice with the given data
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
