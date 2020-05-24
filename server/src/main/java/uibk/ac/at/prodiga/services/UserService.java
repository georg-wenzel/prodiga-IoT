package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.TeamRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.utils.Constants;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

@Component
@Scope("application")
public class UserService {

    private final UserRepository userRepository;
    private final LogInformationService logInformationService;
    private final TeamRepository teamRepository;
    private final ProdigaUserLoginManager userLoginManager;
    private final DiceService diceService;
    private final BookingService bookingService;
    private final BadgeDBService badgeDBService;

    public UserService(UserRepository userRepository, LogInformationService logInformationService, TeamRepository teamRepository, ProdigaUserLoginManager userLoginManager, @Lazy DiceService diceService, @Lazy BookingService bookingService, @Lazy BadgeDBService badgeDBService) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.userLoginManager = userLoginManager;
        this.logInformationService = logInformationService;
        this.diceService = diceService;
        this.bookingService = bookingService;
        this.badgeDBService = badgeDBService;
    }

    /**
     * Returns a collection of all users.
     *
     * @return collection of all users
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public Collection<User> getAllUsers() {
        return Lists.newArrayList(userRepository.findAll());
    }

    public Collection<User> getAllUsersUnauthorized() {
        return Lists.newArrayList(userRepository.findAll());
    }

    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public Collection<User> getAllUsersOfDepartment(Department department) {
        return Lists.newArrayList(userRepository.findDepartmentMemberOf(department));
    }

    /**
     * Returns the number of users in the system
     *
     * @return Number of all users
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')") //NOSONAR
    public int getNumUsers() {
        return Lists.newArrayList(userRepository.findAll()).size();
    }
     /* Loads a single user identified by its username.
     *
     * @param username the username to search for
     * @return the user with the given username
     */
    @PreAuthorize("hasAuthority('ADMIN') or principal.username eq #username") //NOSONAR
    public User loadUser(String username) {
        return userRepository.findFirstByUsername(username);
    }

    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public User saveUser(User user) throws ProdigaGeneralExpectedException
    {
        if(user.getUsername() == null || user.getUsername().isEmpty())
        {
            throw new ProdigaGeneralExpectedException("Username cannot be empty.", MessageType.ERROR);
        }

        //Check team and department consistency
        if(user.getAssignedTeam() != null && !user.getAssignedTeam().getDepartment().equals(user.getAssignedDepartment()))
        {
            throw new ProdigaGeneralExpectedException("Assigned Team and Department of the user do not match up.", MessageType.ERROR);
        }

        if (user.isNew())
        {
            if(userRepository.findFirstByUsername(user.getUsername()) != null) {
                throw new ProdigaGeneralExpectedException("User with same username already exists.", MessageType.WARNING);
            }

            if(!user.getEmail().isEmpty() && userRepository.findFirstByEmail(user.getEmail()).isPresent()) {
                throw new ProdigaGeneralExpectedException("User with same email already exists.", MessageType.WARNING);
            }

            user.setPassword(Constants.PASSWORD_ENCODER.encode(user.getPassword()));

            user.setCreateDate(new Date());
            user.setCreateUser(getAuthenticatedUser());
        }
        else
        {
            User dbUser = userRepository.findFirstByUsername(user.getUsername());

            //If team changed, revoke teamleader role if previously held
            Set<UserRole> roles = user.getRoles();
            if(dbUser.getAssignedTeam() != null && !dbUser.getAssignedTeam().equals(user.getAssignedTeam()))
            {
                roles.remove(UserRole.TEAMLEADER);
            }

            //If department changed, revoke department leader role if previously held
            if(dbUser.getAssignedDepartment() != null && !dbUser.getAssignedDepartment().equals(user.getAssignedDepartment()))
            {
                roles.remove(UserRole.DEPARTMENTLEADER);
            }
            user.setRoles(roles);

            user.setUpdateDate(new Date());
            user.setUpdateUser(getAuthenticatedUser());
        }

        if(user.getRoles() != null) {
            user.getRoles().add(UserRole.EMPLOYEE);
        }

        User result = userRepository.save(user);

        logInformationService.logForCurrentUser("User " + user.getUsername() + " was saved");

        return result;
    }

    /**
     * Deletes the user.
     *
     * @param user the user to delete
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public void deleteUser(User user) throws Exception {
        if(user == null) {
            return;
        }
        checkForUserDeletionOrDeactivation(user);

        Dice d = diceService.getDiceByUser(user);

        if(d != null) {
            bookingService.deleteBookingsForDice(d);
            d.setActive(false);
            d.setUser(null);
            diceService.save(d);
        }

        badgeDBService.deleteBadgesForUser(user);

        userRepository.delete(user);
        logInformationService.logForCurrentUser("User " + user.getUsername() + " was deleted!");
    }

    public void checkForUserDeletionOrDeactivation(User user) throws ProdigaGeneralExpectedException {
        if(user.getUsername().equals(getAuthenticatedUser().getUsername())){
            throw new ProdigaGeneralExpectedException("You can't delete/deactivate your own user account", MessageType.WARNING);
        }
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findFirstByUsername(auth.getName());
    }

    /**
     * Compares a user object with the database, and checks whether it is unchanged from the DB state.
     * @param user The user object to compare
     * @return A boolean signifying whether the user object is unchanged from the database.
     */
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('DEPARTMENTLEADER')") //NOSONAR
    public boolean isUserUnchanged(User user)
    {
        return user.equals(userRepository.findFirstByUsername(user.getUsername()));
    }

    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('TEAMLEADER') || hasAuthority('DEPARTMENTLEADER')") //NOSONAR
    public Collection<User> getUsersByTeam(Team team)
    {
        return Lists.newArrayList(userRepository.findAllByAssignedTeam(team));
    }

    /**
     * Returns all users in the given department
     * @param d The department to look for
     * @return A list with users
     */
    @PreAuthorize("hasAnyAuthority('ADMIN') || hasAuthority('DEPARTMENTLEADER')") //NOSONAR
    public Collection<User> getUsersByDepartment(Department d){
        return Lists.newArrayList(userRepository.findAllByAssignedDepartment(d));
    }

    /**
     * Get all users in the same department as the calling user
     * @return A list of users
     */
    @PreAuthorize("hasAnyAuthority('ADMIN') || hasAuthority('DEPARTMENTLEADER')") //NOSONAR
    public Collection<User> getUsersByDepartment()
    {
        return Lists.newArrayList(userRepository.findAllByAssignedDepartment(userLoginManager.getCurrentUser().getAssignedDepartment()));
    }

    /**
     * Get all users in the same team as the calling user
     * @return A list of users
     */
    @PreAuthorize("hasAnyAuthority('ADMIN') || hasAuthority('TEAMLEADER')") //NOSONAR
    public Collection<User> getUsersByTeam()
    {
        return Lists.newArrayList(userRepository.findAllByAssignedTeam(userLoginManager.getCurrentUser().getAssignedTeam()));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public User getDepartmentLeaderOf(Department department)
    {
        return userRepository.findDepartmentLeaderOf(department);
    }

    @PreAuthorize("hasAuthority('DEPARTMENTLEADER') || hasAuthority('ADMIN')") //NOSONAR
    public User getTeamLeaderOf(Team team)
    {
        return userRepository.findTeamLeaderOf(team);
    }


    /**
     * Assigns a team to a user
     * @param user The user
     * @param team The team
     * @return The user after he was changed in the database
     * @throws ProdigaGeneralExpectedException Is thrown when team to assign and the users department in the DB do not match up.
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER') || hasAuthority('ADMIN')") //NOSONAR
    public User assignTeam(User user, Team team) throws ProdigaGeneralExpectedException
    {
        User dbUser = userRepository.findFirstByUsername(user.getUsername());
        if(dbUser.getAssignedDepartment() != null && team != null && dbUser.getAssignedDepartment().equals(team.getDepartment()))
        {
            if(dbUser.getAssignedTeam() != null && dbUser.getAssignedTeam().equals(team)) return dbUser;

            dbUser.setAssignedTeam(team);
            Set<UserRole> roles = dbUser.getRoles();
            roles.remove(UserRole.TEAMLEADER);
            roles.add(UserRole.EMPLOYEE);
            dbUser.setRoles(roles);
            User result = userRepository.save(dbUser);

            logInformationService.logForCurrentUser("User " + user.getUsername() + " assigned to Team " + team.getName());

            return result;
        }
        else if (team == null)
        {
            dbUser.setAssignedTeam(null);
            Set<UserRole> roles = dbUser.getRoles();
            roles.remove(UserRole.TEAMLEADER);
            roles.add(UserRole.EMPLOYEE);
            dbUser.setRoles(roles);
            User result = userRepository.save(dbUser);

            logInformationService.logForCurrentUser("User " + user.getUsername() + " removed from previous team.");

            return result;
        }
        else
        {
            throw new ProdigaGeneralExpectedException("User team does not match assigned department.", MessageType.ERROR);
        }
    }

    /**
     * Assigns a team to a user
     * @param username The user's name
     * @param teamId The team's id
     * @return The user after he was changed in the database
     * @throws ProdigaGeneralExpectedException Is thrown when team to assign and the users department in the DB do not match up.
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER') || hasAuthority('ADMIN')") //NOSONAR
    public User assignTeam(String username, Long teamId) throws ProdigaGeneralExpectedException
    {
        User dbUser = userRepository.findFirstByUsername(username);
        Team team = teamRepository.findFirstById(teamId);

        return assignTeam(dbUser, team);
    }

     /**
     * Assigns a department to a user
     * @param user The user
     * @param department The department
     * @return The user after he was changed in the database
     * @throws ProdigaGeneralExpectedException Is thrown when saveUser assignemnt was unsuccessful.
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public User assignDepartment(User user, Department department) throws ProdigaGeneralExpectedException
    {
        User dbUser = userRepository.findFirstByUsername(user.getUsername());
        dbUser.setAssignedDepartment(department);
        User result = this.saveUser(dbUser);

        logInformationService.logForCurrentUser("User " + user.getUsername() + " assigned to Department " + department.getName());

        return result;
    }

    /**
     * Sets the historic data editing flag of the user provided
     * @param user The username of the user
     * @param allowedToEdit Whether or not he is allowed to edit historic data
     */
    @PreAuthorize("hasAuthority('TEAMLEADER') || hasAuthority('DEPARTMENTLEADER')")
    public void setEditAllowed(String user, boolean allowedToEdit) throws ProdigaGeneralExpectedException
    {
        User currentUser = userLoginManager.getCurrentUser();
        User dbUser = userRepository.findFirstByUsername(user);
        if(dbUser == null) throw new ProdigaGeneralExpectedException("Could not find user to set flag for.", MessageType.ERROR);

        if(currentUser.getRoles().contains(UserRole.DEPARTMENTLEADER))
        {
            if(dbUser.getAssignedDepartment().equals(currentUser.getAssignedDepartment()))
            {
                dbUser.setMayEditHistoricData(allowedToEdit);
                userRepository.save(dbUser);
            }
            else
            {
                throw new RuntimeException("Illegal attempt to set flag of user that is out of authorization scope.");
            }
        }

        else if(currentUser.getRoles().contains(UserRole.TEAMLEADER))
        {
            if(dbUser.getAssignedTeam().equals(currentUser.getAssignedTeam()))
            {
                dbUser.setMayEditHistoricData(allowedToEdit);
                userRepository.save(dbUser);
            }
            else
            {
                throw new RuntimeException("Illegal attempt to set flag of user that is out of authorization scope.");
            }
        }
    }

    /**
     * Returns a collection of all user roles in the system
     * @return A collection of all user roles
     */
    public Collection<UserRole> getAllUserRoles()
    {
        return Arrays.asList(UserRole.values());
    }

    /**
     * Returns a newly created user
     *
     * @return A newly created user entity
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public User createNewUser() {
        return new User();
    }
}