package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.FrequencyType;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.services.UserService;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;
import uibk.ac.at.prodiga.utils.SnackbarHelper;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("view")
public class UserDetailController {

    private final UserService userService;
    private final ProdigaUserLoginManager userLoginManager;

    /**
     * Attribute to cache the currently displayed user
     */
    private User user;

    public UserDetailController(UserService userService, ProdigaUserLoginManager userLoginManager) {
        this.userService = userService;
        this.userLoginManager = userLoginManager;
    }

    /**
     * Sets the currently displayed user and reloads it form db. This user is
     * targeted by any further calls of
     *
     *
     * @param user
     */
    public void setUser(User user) throws Exception {
        this.user = user;
        doReloadUser(user.getUsername());
    }

    /**
     * Returns the currently displayed user.
     *
     * @return
     */
    public User getUser() {
        return user;
    }

    /**
     * Reloads the given user. If {@param username} is empty a new user will be created
     *
     * @param username The name of the user to reload
     */
    public void doReloadUser(String username) throws Exception {
        if (username != null && !username.trim().isEmpty()) {
            this.user = userService.loadUser(username);
        } else {
            this.user = userService.createNewUser();
        }
    }

    /**
     * Action to save the currently displayed user.
     */
    public void doSaveUser() throws Exception {
        user = this.userService.saveUser(user);
        SnackbarHelper.getInstance()
                .showSnackBar("User " + user.getUsername() + " saved!", MessageType.INFO);
    }

    /**
     * Action to delete the currently displayed user.
     */
    public void doDeleteUser() throws Exception {
        this.userService.deleteUser(user);
        SnackbarHelper.getInstance()
                .showSnackBar("User " + user.getUsername() + " deleted!", MessageType.ERROR);
    }

    public List<String> getAllRoles() {
        List<String> userRoleList = new LinkedList<>();

        if(this.user.getRoles().contains(UserRole.ADMIN)){
            userRoleList.add(UserRole.ADMIN.getLabel());
        }
        if(this.user.getRoles().contains(UserRole.DEPARTMENTLEADER)){
            userRoleList.add(UserRole.DEPARTMENTLEADER.getLabel());
        }
        if(this.user.getRoles().contains(UserRole.TEAMLEADER)){
            userRoleList.add(UserRole.TEAMLEADER.getLabel());
        }
        if(this.user.getRoles().contains(UserRole.EMPLOYEE)){
            userRoleList.add(UserRole.EMPLOYEE.getLabel());
        }
        return userRoleList;
    }

    /**
     * Returns a list of all existing user roles
     * @return list of all existing user roles
     */
    public List<String> getAllRolesTotal() {
        List<String> userRoleList = new LinkedList<>();
        userRoleList.add(UserRole.ADMIN.getLabel());
        userRoleList.add(UserRole.DEPARTMENTLEADER.getLabel());
        userRoleList.add(UserRole.TEAMLEADER.getLabel());
        userRoleList.add(UserRole.EMPLOYEE.getLabel());
        return userRoleList;
    }

    /**
     * The the current user name
     * @return The current user name
     */
    public String getUserByName() {
        if(this.user == null) {
            return null;
        }
        return user.getUsername();
    }

    /**
     * Sets the current user based on username
     *
     * @param username The username
     */
    public void setUserByName(String username) throws Exception {
        doReloadUser(username);
    }

    /**
     * Returns a list of all existing frequency types
     * @return all frequency types
     */
    public List<FrequencyType> getAllFrequencyTypesTotal() {
        List<FrequencyType> freqencyTypeList = new LinkedList<>();
        freqencyTypeList.add(FrequencyType.DAILY);
        freqencyTypeList.add(FrequencyType.WEEKLY);
        freqencyTypeList.add(FrequencyType.MONTHLY);
        return freqencyTypeList;
    }

    /**
     *  Sets the Admin Role for when a new Admin is created.
     *
     * @param isAdmin
     */
    public void setIsAdmin(boolean isAdmin) {
        if(user.isNew()){
            if(isAdmin) {
                Set<UserRole> userRoles = new HashSet<>();
                userRoles.add(UserRole.ADMIN);
                this.user.setRoles(userRoles);
            }
        }
    }

    /**
     *  Is needed for isAdmin Checkbox.
     *  Set to false if user is new.
     *
     * @return true if user is admin
     */
    public boolean getIsAdmin() {
        if(user.isNew()){
            return false;
        }
        return this.user.getRoles().contains(UserRole.ADMIN);
    }

}
