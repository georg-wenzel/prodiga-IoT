package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.services.UserService;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

@Component
@Scope("view")
public class UserDetailController {

    private final UserService userService;

    /**
     * Attribute to cache the currently displayed user
     */
    private User user;

    public UserDetailController(UserService userService) {
        this.userService = userService;
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
    public void doSaveUser() throws ProdigaGeneralExpectedException {
        user = this.userService.saveUser(user);
    }

    /**
     * Action to delete the currently displayed user.
     */
    public void doDeleteUser() throws Exception {
        this.userService.deleteUser(user);
        user = null;
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

}
