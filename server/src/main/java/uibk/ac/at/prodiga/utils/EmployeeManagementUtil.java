package uibk.ac.at.prodiga.utils;

import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;

/**
 * Utility class for employee management, e.g. in setting department and teams.
 */
public class EmployeeManagementUtil
{
    /**
     * Returns true if the user does not have teamleader or departmentleader privileges
     * User state is kept from the user object u - it is not reloaded from the database.
     * @param u The user object to check.
     * @return True if the user does not have TEAMLEADER or DEPARTMENTLEADER roles.
     */
    public static boolean isSimpleEmployee(User u)
    {
        return !(u.getRoles().contains(UserRole.DEPARTMENTLEADER) || u.getRoles().contains(UserRole.TEAMLEADER));
    }
}
