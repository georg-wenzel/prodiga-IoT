package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Vacation;
import uibk.ac.at.prodiga.services.VacationService;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;
import uibk.ac.at.prodiga.utils.SnackbarHelper;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;

/**
 * Controller for managing the Vacations view
 */
@Component
@Scope("view")
public class VacationController
{
    private final VacationService vacationService;
    private final ProdigaUserLoginManager userLoginManager;
    private Vacation vacation;

    public VacationController(VacationService vacationService, ProdigaUserLoginManager userLoginManager)
    {
        this.vacationService = vacationService;
        this.userLoginManager = userLoginManager;
    }

    /**
     * Returns a collection of all vacations for the user
     * @return A collection of all vacations for the user
     */
    public Collection<Vacation> getAllVacations() {
        return vacationService.getAllVacations();
    }

    /**
     * Saves currently selected vacation
     * @throws ProdigaGeneralExpectedException when an error occurs during saving
     */
    public void doSaveVacation() throws ProdigaGeneralExpectedException
    {
        if(vacation == null) throw new RuntimeException("No vacation found to save.");

        vacation.setUser(userLoginManager.getCurrentUser());
        //add 5 hours offset, because of some sketchy time zone stuff...
        vacation.setBeginDate(Date.from(Instant.ofEpochMilli(vacation.getBeginDate().getTime() + 1000 * 60 * 60 * 5)));
        vacation.setEndDate(Date.from(Instant.ofEpochMilli(vacation.getEndDate().getTime() + 1000 * 60 * 60 * 5)));

        vacation = vacationService.saveVacation(vacation);
        SnackbarHelper.getInstance().showSnackBar("Vacation from " + vacation.getBeginDate() + " to " + vacation.getEndDate() + " saved!", MessageType.INFO);
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
        this.vacationService.deleteVacation(vacation);
        SnackbarHelper.getInstance()
                .showSnackBar("Vacation \"" + vacation.getId() + "\" deleted!", MessageType.ERROR);
    }
}
