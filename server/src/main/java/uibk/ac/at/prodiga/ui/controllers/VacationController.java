package uibk.ac.at.prodiga.ui.controllers;

import de.jollyday.Holiday;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Vacation;
import uibk.ac.at.prodiga.services.VacationService;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;
import uibk.ac.at.prodiga.utils.SnackbarHelper;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing the Vacations view
 */
@Component
@Scope("view")
public class VacationController implements Serializable
{
    private static final long serialVersionUID = 5325687637192577315L;

    private final VacationService vacationService;
    private final ProdigaUserLoginManager userLoginManager;
    private Vacation vacation;

    private List<Vacation> pastVacations;
    private List<Vacation> currentVacations;
    private List<Holiday> holidays;

    public VacationController(VacationService vacationService, ProdigaUserLoginManager userLoginManager)
    {
        this.vacationService = vacationService;
        this.userLoginManager = userLoginManager;
    }

    /**
     * Returns a collection of all vacations for the user that are ongoing or in the future
     * @return A collection of all vacations for the user that are ongoing or in the future
     */
    public Collection<Vacation> getCurrentVacations() {
       if(currentVacations == null)
           currentVacations = vacationService.getAllVacations().stream().filter(v -> v.getEndDate().after(new Date())).collect(Collectors.toList());
        return currentVacations;
    }

    /**
     * Returns a collection of all vacations for the user that are in the past
     * @return A collection of all vacations for the user that are in the past
     */
    public Collection<Vacation> getPastVacations() {
        if(pastVacations == null)
            pastVacations = vacationService.getAllVacations().stream().filter(v -> v.getEndDate().before(new Date()) && this.vacationService.toLocalDate(v.getEndDate()).getYear() == LocalDate.now().getYear()).collect(Collectors.toList());
        return pastVacations;
    }

    /**
     * Saves currently selected vacation
     * @throws ProdigaGeneralExpectedException when an error occurs during saving
     */
    public void doSaveVacation() throws ProdigaGeneralExpectedException
    {
        if(vacation == null) throw new RuntimeException("No vacation found to save.");
        vacation.setUser(userLoginManager.getCurrentUser());

        //Normalize the date to be at midnight UTC
        vacation.setBeginDate(Date.from(Instant.ofEpochMilli(vacation.getBeginDate().getTime() + ZoneOffset.systemDefault().getRules().getOffset(Instant.now()).getTotalSeconds() * 1000)));
        vacation.setEndDate(Date.from(Instant.ofEpochMilli(vacation.getEndDate().getTime() + ZoneOffset.systemDefault().getRules().getOffset(Instant.now()).getTotalSeconds() * 1000)));

        Vacation save_vacation = vacationService.saveVacation(vacation);
        //update if vacation was saved successfully
        if(save_vacation != null) vacation = save_vacation;
        SnackbarHelper.getInstance().showSnackBar("Vacation from " + this.vacationService.toLocalDate(vacation.getBeginDate()) + " to " + this.vacationService.toLocalDate(vacation.getEndDate()) + " saved!", MessageType.INFO);
    }

    /**
     * Safely gets vacation ID
     *
     * @return the vacation by id
     */
    public Long getVacationById()
    {
        if(this.vacation == null)
            this.vacation = new Vacation();

        return this.vacation.getId();
    }

    /**
     * Sets currently active vacation by the id
     * @param vacationId Vacation ID
     */
    public void setVacationById(Long vacationId)
    {
        this.vacation = vacationService.getVacationById(vacationId);
        if(this.vacation == null)
            this.vacation = new Vacation();
    }

    /**
     * Gets vacation property
     *
     * @return the vacation
     */
    public Vacation getVacation() {
        if(this.vacation == null)
            this.vacation = new Vacation();
        return this.vacation;
    }

    /**
     * Sets vacation property
     * @param vacation The vacation to set
     */
    public void setVacation(Vacation vacation)
    {
        if(vacation == null) this.vacation = new Vacation();
        else this.vacation = vacation;

        if(this.vacation.getId() != null) setVacationById(this.vacation.getId());
    }

    /**
     * Deletes the vacation.
     * @throws Exception is thrown when an error occurs
     */
    public void doDeleteVacation() throws Exception
    {
        this.vacationService.deleteVacation(vacation, false);
        this.currentVacations.remove(vacation);
        SnackbarHelper.getInstance()
                .showSnackBar("Vacation \"" + vacation.getId() + "\" deleted!", MessageType.ERROR);
    }

    /**
     * Returns remaining vacation days of this year
     * @return The remaining vacation days for this year.
     */
    public int getCurrentYearDays()
    {
        return this.vacationService.getUsersRemainingVacationDays(LocalDate.now().getYear());
    }

    /**
     * Returns remaining vacation days of the upcoming year
     * @return The remaining vacation days for the upcoming year.
     */
    public int getNextYearDays()
    {
        return this.vacationService.getUsersRemainingVacationDays(LocalDate.now().plusYears(1).getYear());
    }

    /**
     * Gets the number of days in a vacation
     * @param vacation The vacation to check
     * @return The length in days of this vacation
     */
    public int getDays(Vacation vacation)
    {
        return this.vacationService.getVacationDays(vacation);
    }

    /**
     * Returns whether or not the vacation has already started
     * @param vacation The vacation to check
     * @return true if the vacation has already started
     */
    public boolean hasStarted(Vacation vacation)
    {
        return vacation.getBeginDate().after(new Date());
    }

    /**
     * Returns whether or not the vacation has already ended
     * @param vacation The vacation to check
     * @return true if the vacation has already ended
     */
    public boolean hasEnded(Vacation vacation)
    {
        return vacation.getEndDate().after(new Date());
    }

    /**
     * Returns whether or not the vacation is being edited or created at the moment
     * @return true if the vacation is being edited, i.e. has an existing database object.
     */
    public boolean getEditing()
    {
        return vacation != null && vacation.getObjectCreatedDateTime() != null;
    }

    /**
     * Gets the holidays of the current year
     * @return a list of holidays from the current year
     */
    public Collection<Holiday> getHolidays() {
        if(holidays == null) holidays = new ArrayList<>(vacationService.getHolidays());
        return holidays;
    }
}
