package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Vacation;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.BookingRepository;
import uibk.ac.at.prodiga.repositories.VacationRepository;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Service for accessing and manipulating vacations.
 */
@Component
@Scope("application")
public class VacationService
{
    private final VacationRepository vacationRepository;
    private final ProdigaUserLoginManager userLoginManager;
    private final BookingRepository bookingRepository;

    public VacationService(VacationRepository vacationRepository, ProdigaUserLoginManager userLoginManager, BookingRepository bookingRepository)
    {
        this.vacationRepository = vacationRepository;
        this.userLoginManager = userLoginManager;
        this.bookingRepository = bookingRepository;
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
            throw new RuntimeException("Attempted to save vacation of different user.");
        }

        //check vacation start time
        if(vacation.getEndDate().before(vacation.getBeginDate()))
        {
            throw new ProdigaGeneralExpectedException("Vacation cannot end before it begins.", MessageType.ERROR);
        }

        if(vacation.getBeginDate().before(new Date()))
        {
            throw new ProdigaGeneralExpectedException("Vacation cannot be set for the past.", MessageType.ERROR);
        }

        //check vacation end time
        LocalDate maxDate = LocalDate.of(LocalDate.now().getYear()+1,12,31);
        if(toLocalDate(vacation.getEndDate()).isAfter(maxDate))
        {
            throw new ProdigaGeneralExpectedException("Vacations can only be set for the current and following year.", MessageType.ERROR);
        }

        //check vacation specifics (see method details)
        checkVacationBelowThresholdAndValid(vacation);

        //set appropriate fields
        if(vacation.isNew())
        {
            vacation.setObjectCreatedDateTime(new Date());
            vacation.setObjectCreatedUser(userLoginManager.getCurrentUser());
        }
        else
        {
            //additionally, if vacation already existed, make sure that the user of the existing database vacation matches, and that it is not a vacation that used to be in the past.
            Vacation db_vacation = vacationRepository.findFirstById(vacation.getId());
            if(!db_vacation.getUser().equals(u))
            {
                throw new RuntimeException("Attempted to edit vacation of different user.");
            }
            if(db_vacation.getBeginDate().before(new Date()))
            {
                throw new ProdigaGeneralExpectedException("Vacations cannot be set for the past", MessageType.ERROR);
            }

            vacation.setObjectChangedDateTime(new Date());
            vacation.setObjectChangedUser(userLoginManager.getCurrentUser());
        }

        //Save method if no exception has been thrown so far
        return vacationRepository.save(vacation);
    }

    /**
     * Loads a single vacation by id
     *
     * @param vacationId The id to search by
     * @return the vacation with the given ID
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public Vacation getVacationById(Long vacationId)
    {
        Vacation v = vacationRepository.findFirstById(vacationId);
        if(v == null) return null;

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
    public void deleteVacation(Vacation vacation) throws ProdigaGeneralExpectedException
    {
        Vacation v = vacationRepository.findFirstById(vacation.getId());
        if(!v.getUser().equals(userLoginManager.getCurrentUser()))
        {
            throw new RuntimeException("Attempted to delete vacation from different user.");
        }
        if(v.getBeginDate().before(new Date()))
        {
            throw new ProdigaGeneralExpectedException("Cannot delete vacations that have already begun or ended.", MessageType.ERROR);
        }
        vacationRepository.delete(v);
    }

    /**
     * Returns an integer containing the number of remaining vacation days in the given year
     * @param year The given year
     * @return The number of remaining vacation days in this year.
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public int getUsersRemainingVacationDays(int year)
    {
        User u = userLoginManager.getCurrentUser();
        return 25 - vacationRepository.findUsersYearlyVacations(u,year).stream().mapToInt(v -> getVacationDaysInYear(v, year)).sum();
    }

    /**
     * Given a vacation, gets all other vacations in the year of the start and end date.
     * The method then checks
     *  - that 25 vacation days are not passed for any year.
     *  - that no other vacation of this user covers the same days
     *  - that no booking is already taken for any of the vacation days.
     * @param vacation the vacation to check
     * @throws ProdigaGeneralExpectedException is thrown with type ERROR if the vacation is not valid in some way.
     */
    private void checkVacationBelowThresholdAndValid(Vacation vacation) throws ProdigaGeneralExpectedException
    {
        //Check vacation duration
        LocalDate startDate = vacation.getBeginDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = vacation.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        //Check remaining days for start year
        checkYearlyVacationDays(vacation, startDate.getYear());

        //Repeat for end year if end year != start year
        if(startDate.getYear() != endDate.getYear())
        {
            checkYearlyVacationDays(vacation, endDate.getYear());
        }

        //Check that there is no other vacations over the same days
        Collection<Vacation> vcs = vacationRepository.findUsersVacationInRange(vacation.getUser(), vacation.getBeginDate(), vacation.getEndDate());
        if(vacation.getId() != null) vcs.remove(vacation);

        if(!vcs.isEmpty())
        {
            throw new ProdigaGeneralExpectedException("Vacation covers existing vacation time.", MessageType.ERROR);
        }

        //check that there is no booking for any vacation days
        if(!bookingRepository.findUsersBookingInRange(vacation.getUser(), vacation.getBeginDate(), vacation.getEndDate()).isEmpty())
        {
            throw new ProdigaGeneralExpectedException("Vacation covers existing bookings.", MessageType.ERROR);
        }
    }

    /**
     * Given a vacation and a year, checks the sum of the vacation user's other vacations in this year and the given vacation to be below 25
     * @param vacation The vacation to calculate the yearly vacation days for
     * @param year The year to calculate the yearly vacation days for
     * @throws ProdigaGeneralExpectedException Is thrown when the vacation days of 25 are exceeded for this year.
     */
    private void checkYearlyVacationDays(Vacation vacation, int year) throws ProdigaGeneralExpectedException
    {
        Collection<Vacation> vcsStart = vacationRepository.findUsersYearlyVacations(vacation.getUser(), year);
        if(vacation.getId() != null) vcsStart.remove(vacation);
        int daysFromOtherVacations = vcsStart.stream().mapToInt(v ->
                getVacationDaysInYear(v, year)).sum();
        //add the vacation on
        int currentVacationTime = getVacationDaysInYear(vacation, year);
        if(daysFromOtherVacations + currentVacationTime > 25)
        {
            throw new ProdigaGeneralExpectedException("Yearly vacation days for the year " + year + " cannot exceed 25.", MessageType.ERROR);
        }
    }

    /**
     * Gets the number of days in a vacation
     * @param vacation The vacation to check
     * @return The number of days between start and end date.
     */
    public int getVacationDays(Vacation vacation)
    {
        return(int)Duration.between(toLocalDate(vacation.getBeginDate()).atStartOfDay(), toLocalDate(vacation.getEndDate()).plusDays(1).atStartOfDay()).toDays();
    }

    /**
     * Gets the number of days in a vacation, but only the days in a given year
     * @param vacation The vacation to check
     * @param year the year bound
     * @return The number of days between start and end date.
     */
    private int getVacationDaysInYear(Vacation vacation, int year)
    {
        LocalDate beginDate = toLocalDate(vacation.getBeginDate());
        LocalDate endDate = toLocalDate(vacation.getEndDate()).plusDays(1);
        LocalDate upperBound = LocalDate.of(year+1,1,1);
        LocalDate lowerBound = LocalDate.of(year,1,1);
        if(beginDate.isBefore(lowerBound)) beginDate = lowerBound;
        if(endDate.isAfter(upperBound)) endDate = upperBound;

        return (int)Duration.between(beginDate.atStartOfDay(), endDate.atStartOfDay()).toDays();
    }

    /**
     * Converts a java.util.Date to a LocalDate
     * @param date the date to convert
     * @return The corresponding LocalDate
     */
    public LocalDate toLocalDate(Date date)
    {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Converts a LocalDate to a java.util.Date
     * @param localDate the date to convert
     * @return The corresponding java.util.Date
     */
    public Date toDate(LocalDate localDate)
    {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
