package uibk.ac.at.prodiga.services;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

@Component
@Scope("application")
public class UserService {

    private final UserRepository userRepository;
    private final LogInformationService logInformationService;

    public UserService(UserRepository userRepository, LogInformationService logInformationService) {
        this.userRepository = userRepository;
        this.logInformationService = logInformationService;
    }

    /**
     * Returns a collection of all users.
     *
     * @return collection of all users
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Collection<User> getAllUsers() {
        return Lists.newArrayList(userRepository.findAll());
    }

    /**
     * Loads a single user identified by its username.
     *
     * @param username the username to search for
     * @return the user with the given username
     */
    @PreAuthorize("hasAuthority('ADMIN') or principal.username eq #username")
    public User loadUser(String username) {
        return userRepository.findFirstByUsername(username);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
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
        return userRepository.save(user);
    }

    /**
     * Deletes the user.
     *
     * @param user the user to delete
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUser(User user) throws Exception {
        checkForUserDeletionOrDeactivation(user);
        userRepository.delete(user);
        logInformationService.log("User " + user.getUsername() + " was deleted!");
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
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('DEPARTMENTLEADER')")
    public boolean isUserUnchanged(User user)
    {
        return user.equals(userRepository.findFirstByUsername(user.getUsername()));
    }

    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('TEAMLEADER')")
    public Collection<User> getUsersByTeam(Team team)
    {
        return Lists.newArrayList(userRepository.findAllByAssignedTeam(team));
    }

    /**
     * Returns all users in the given department
     * @param d The department to look for
     * @return A list with users
     */
    @PreAuthorize("hasAnyAuthority('ADMIN') || hasAuthority('DEPARTMENTLEADER')")
    public Collection<User> getUsersByDepartment(Department d){
        return Lists.newArrayList(userRepository.findAllByAssignedDepartment(d));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public User getDepartmentLeaderOf(Department department)
    {
        return userRepository.findDepartmentLeaderOf(department);
    }

    @PreAuthorize("hasAuthority('DEPARTMENTLEADER') || hasAuthority('ADMIN')")
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
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER') || hasAuthority('ADMIN')")
    public User assignTeam(User user, Team team) throws ProdigaGeneralExpectedException
    {
        User dbUser = userRepository.findFirstByUsername(user.getUsername());
        if(dbUser.getAssignedDepartment() != null && dbUser.getAssignedDepartment().equals(team.getDepartment()))
        {
            if(dbUser.getAssignedTeam() != null && dbUser.getAssignedTeam().equals(team)) return dbUser;

            dbUser.setAssignedTeam(team);
            Set<UserRole> roles = dbUser.getRoles();
            roles.remove(UserRole.TEAMLEADER);
            roles.add(UserRole.EMPLOYEE);
            dbUser.setRoles(roles);
            return userRepository.save(dbUser);
        }
        else
        {
            throw new ProdigaGeneralExpectedException("User team does not match assigned department.", MessageType.ERROR);
        }
    }

     /**
     * Assigns a department to a user
     * @param user The user
     * @param department The department
     * @return The user after he was changed in the database
     * @throws ProdigaGeneralExpectedException Is thrown when saveUser assignemnt was unsuccessful.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public User assignDepartment(User user, Department department) throws ProdigaGeneralExpectedException
    {
        User dbUser = userRepository.findFirstByUsername(user.getUsername());
        dbUser.setAssignedDepartment(department);
        return this.saveUser(dbUser);
    }

    /**
     * Returns a newly created user
     *
     * @return A newly created user entity
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public User createNewUser() {
        return new User();
    }
}
