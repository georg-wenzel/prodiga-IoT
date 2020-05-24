package uibk.ac.at.prodiga.ui.controllers;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.Vacation;
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.services.VacationService;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;

@Component
@Scope("view")
public class CalendarController implements Serializable {

    private static final long serialVersionUID = 5325687687692577565L;

    private ScheduleModel eventModel;
    private ScheduleEvent event = new DefaultScheduleEvent();
    private String locale = "de";

    public CalendarController(VacationService vacationService, BookingService bookingService) {

        eventModel = new DefaultScheduleModel();

        Collection<Vacation> vacations = vacationService.getAllVacations();
        Collection<Booking> bookings = bookingService.getAllBookingsByCurrentUser();

        vacations.forEach(x ->
        {
            Instant endInstant = x.getEndDate().toInstant();
            Date newEnd = Date.from(endInstant.plus(1, ChronoUnit.DAYS));

            DefaultScheduleEvent vacation = new DefaultScheduleEvent("Vacation", x.getBeginDate(), newEnd, true);
            vacation.setData(x);
            eventModel.addEvent(vacation);
        });

        bookings.forEach(x ->
        {
            Instant endInstant = x.getActivityEndDate().toInstant();
            Date newEnd = Date.from(endInstant.plus(1, ChronoUnit.DAYS));

            DefaultScheduleEvent booking = new DefaultScheduleEvent(x.getBookingCategory().getName(), x.getActivityStartDate(), newEnd, true);
            booking.setData(x);
            booking.setAllDay(false);
            eventModel.addEvent(booking);
        });
    }

    /**
     * Returns the current model for the calendar
     * @return The calendar model
     */
    public ScheduleModel getEventModel() {
        return eventModel;
    }


    /**
     * Returns the currently selected Event
     * @return The currently selected Event
     */
    public ScheduleEvent getEvent() {
        return event;
    }

    /**
     * Sets the currently selected Event
     * @param event The new event
     */
    public void setEvent(ScheduleEvent event) {
        this.event = event;
    }

    /**
     * Triggered when the user chooses a new event
     * @param selectEvent The new event
     */
    public void onEventSelect(SelectEvent selectEvent) {
        event = (ScheduleEvent) selectEvent.getObject();
    }

    /**
     * Gets the locale of the calendar
     * @return The calendar locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the calendar locale
     * @param locale The new locale
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }
}
