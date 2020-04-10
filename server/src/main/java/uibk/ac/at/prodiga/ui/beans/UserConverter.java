package uibk.ac.at.prodiga.ui.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uibk.ac.at.prodiga.model.User;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.HashMap;
import java.util.Map;

/**
 * Here comes the fun part.
 * Since creating a Dropdown menu in this framework using an entity is basically impossible
 * we create this converter here.
 * And since we can't use DI here - which DOES NOT MAKE SENSE AT ALL (like not even in the slightest
 * because this class is a super special don't touch anything class) - we have to cache all users here
 * Seems to be faster than using DI anyways - which is totally odd because calling a method on a
 * APPLICATION wide object must go through like 20 call stacks which internally call like 10
 * reflection methods. Remember kids 3 Billion Devices Run Slowly
 *
 * It contains A dictionary of all users which are available in the dropdown grouped by their username
 * Once the user hits safe we retrieve them from the Dict.
 */
@FacesConverter("userConverter")
@Component
@Scope("session")
public class UserConverter implements Converter {

    private Map<String, User> usersPerUserName = new HashMap<>();

    /**
     * Returns the specific user for with the given username
     * @param facesContext Current context
     * @param uiComponent Component which calls method
     * @param s The users username
     * @return A user object
     */
    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        if(StringUtils.isEmpty(s)) {
            return null;
        }

        return usersPerUserName.getOrDefault(s, null);
    }

    /**
     * Returns the username for the given user
     * @param facesContext The current context
     * @param uiComponent The current component
     * @param o The user object
     * @return Users username
     */
    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if(o instanceof User) {
            User u = (User)o;

            usersPerUserName.put(u.getUsername(), u);

            return u.getUsername();
        }

        return null;
    }
}
