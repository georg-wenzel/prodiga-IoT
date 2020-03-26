package uibk.ac.at.prodiga.services;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.utils.AsyncHelper;
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
     * @return
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

    /**
     * Saves the user. This method will also set {@link User#createDate} for new
     * entities or {@link User#updateDate} for updated entities. The user
     * requesting this operation will also be stored as {@link User#createDate}
     * or {@link User#updateUser} respectively.
     *
     * @param user the user to save
     * @return the updated user
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public User saveUser(User user) {
        if (user.isNew()) {
            user.setCreateDate(new Date());
            user.setCreateUser(getAuthenticatedUser());
        } else {
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
        userRepository.delete(user);
        logInformationService.log("User " + user.getUsername() + " was deleted!");
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
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('DEPARTMENTLEADER') || hasAuthority('TEAMLEADER') || hasAuthority('EMPLOYEE')")
    public boolean isUserUnchanged(User user)
    {
        return user.equals(userRepository.findFirstByUsername(user.getUsername()));
    }

    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('DEPARTMENTLEADER') || hasAuthority('TEAMLEADER')")
    public Collection<User> getUsersByTeam(Team team)
    {
        return Lists.newArrayList(userRepository.findAllByAssignedTeam(team));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public User getDepartmentLeaderOf(Department department)
    {
        return userRepository.findDepartmentLeaderOf(department);
    }

    @PreAuthorize("hasAuthority('DEPARTMENTLEADER')")
    public User getTeamLeaderOf(Team team)
    {
        return userRepository.findTeamLeaderOf(team);
    }
}
