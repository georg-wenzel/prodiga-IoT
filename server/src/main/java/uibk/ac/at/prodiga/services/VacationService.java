package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Vacation;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.VacationRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.utils.EmployeeManagementUtil;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Service for accessing and manipulating vacations.
 */
@Component
@Scope("application")
public class VacationService
{
    private final VacationRepository vacationRepository;
    private final ProdigaUserLoginManager userLoginManager;

    public VacationService(VacationRepository vacationRepository, ProdigaUserLoginManager userLoginManager)
    {
        this.vacationRepository = vacationRepository;
        this.userLoginManager = userLoginManager;
    }

    /**
     * Returns a collection of all the users
     * @return A collection of all vacations of the user calling the method
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public Collection<Vacation> getAllVacations()
    {
        User u = userLoginManager.getCurrentUser();
        return Lists.newArrayList(vacationRepository.findAllByUser(u));
    }

    /**
     * Saves a vacation in the database. If an object with this ID already exists, overwrites the object's data at this ID
     * @param vacation The vacation to save
     * @return The new state of the object in the database.
     * @throws ProdigaGeneralExpectedException Is thrown when data is not valid, e.g. dates do not work out or vacation user is not currently logged in user.
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public Vacation saveVacation(Vacation vacation) throws ProdigaGeneralExpectedException
    {
        User u = userLoginManager.getCurrentUser();
        //Make sure user is correct
        if(!vacation.getUser().equals(u))
        {
            throw new RuntimeException("Attempted to save vacation of different user");
        }

        //check fields
        if(vacation.getEndDate().before(vacation.getBeginDate()))
        {
            throw new ProdigaGeneralExpectedException("Vacation cannot end before it begins", MessageType.ERROR);
        }

        //Get vacation duration
        LocalDate startDate = vacation.getBeginDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = vacation.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Duration d = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay());
        if(d.toDays() < 1 || d.toDays() > 35)
        {
            throw new ProdigaGeneralExpectedException("Vacation must be between 1 and 35 days", MessageType.ERROR);
        }

        //set appropriate fields
        if(vacation.isNew())
        {
            vacation.setObjectCreatedDateTime(new Date());
            vacation.setObjectCreatedUser(userLoginManager.getCurrentUser());
        }
        else
        {
            vacation.setObjectChangedDateTime(new Date());
            vacation.setObjectChangedUser(userLoginManager.getCurrentUser());
        }
        return vacationRepository.save(vacation);
    }

    /**
     * Loads a single vacation by id
     *
     * @param vacationId The id to search by
     * @return the vacation with the given ID
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public Vacation loadVacation(Long vacationId)
    {
        Vacation v = vacationRepository.findFirstById(vacationId);
        if(!v.getUser().equals(userLoginManager.getCurrentUser()))
        {
            throw new RuntimeException("Attempted to load vacation from different user.");
        }
        return v;
    }

    /**
     * Deletes the vacation.
     *
     * @param vacation the vacation to delete
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public void deleteVacation(Vacation vacation)
    {
        Vacation v = vacationRepository.findFirstById(vacation.getId());
        if(!v.getUser().equals(userLoginManager.getCurrentUser()))
        {
            throw new RuntimeException("Attempted to delete vacation from different user.");
        }
        vacationRepository.delete(vacation);
    }
}
