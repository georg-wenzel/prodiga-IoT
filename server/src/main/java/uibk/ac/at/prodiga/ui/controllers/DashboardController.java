package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.BadgeDB;
import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.services.*;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.awt.print.Book;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Scope("application")
public class DashboardController implements Serializable
{
    private static final long serialVersionUID = 5325687687692577315L;

    private final DepartmentService departmentService;
    private final RaspberryPiService raspberryPiService;
    private final UserService userService;
    private final TeamService teamService;
    private final BookingService bookingService;
    private final ProdigaUserLoginManager userLoginManager;
    private final BadgeDBService badgeDBService;


    public DashboardController(DepartmentService departmentService, RaspberryPiService raspberryPiService, UserService userService, TeamService teamService, BookingService bookingService, ProdigaUserLoginManager userLoginManager, BadgeDBService badgeDBService)
    {
        this.departmentService = departmentService;
        this.raspberryPiService = raspberryPiService;
        this.userService = userService;
        this.teamService = teamService;
        this.bookingService = bookingService;
        this.userLoginManager = userLoginManager;
        this.badgeDBService = badgeDBService;
    }

    /**
     * Gets the number of all departments.
     *
     * @return number of all departments.
     */
    public int numDepartments(){
        return departmentService.getNumDepartments();
    }

    /**
     * Gets the number of all configured raspberry pis.
     * @return number of all configured raspberry pis.
     */
    public int numRaspberryPis(){
        return this.raspberryPiService.getNumConfiguredRaspberryPis();
    }

    /**
     * Gets the number of all users.
     *
     * @return number of total users.
     */
    public int numUsers(){
        return userService.getNumUsers();
    }

    /**
     * Gets the number of all teams.
     * @return number of teams.
     */
    public int numTeams(){
        return this.teamService.getNumTeams();
    }

    /**
     * Gets the number of badges for last week.
     * @return number of badges for last week.
     */
    public int numBadgesLastWeek() {
        Collection<BadgeDB> badgeDBS = badgeDBService.getLastWeeksBadgesByUser(userLoginManager.getCurrentUser());
        return badgeDBS.size();
    }

    /**
     * Gets the total number of badges.
     * @return total number of badges.
     */
    public int numBadgesTotal() {
        Collection<BadgeDB> badgeDBS = badgeDBService.getAllBadgesByUser(userLoginManager.getCurrentUser());
        return badgeDBS.size();
    }

    /**
     * Helper function for getting working hours.
     * @param bookings
     * @return number of working hours
     */
    public long workingHoursInRange(Collection<Booking> bookings) {
        long time = 0;
        for(Booking booking : bookings){
            if(booking.getBookingCategory().getName().equals("Pause / Vacation")){
                continue;
            }
            time += (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime());
        }
        return TimeUnit.MILLISECONDS.toHours(time);
    }


    /**
     * Gets the total amount of working hours for the current week.
     * @return hours of working for this week.
     */
    public long workingHoursThisWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date beginning = cal.getTime();

        Collection<Booking> bookings = bookingService.getBookingInRangeForUser(userLoginManager.getCurrentUser(), beginning,new Date());
        return workingHoursInRange(bookings);
    }

    /**
     * Gets the total amount of working hours for the last week.
     * @return hours of working for last week.
     */
    public long workingHoursLastWeek() {
        Calendar cal = badgeDBService.getWeekBeginning();
        cal.add(Calendar.DATE, -7);
        Date start = cal.getTime();

        Calendar cal2 = badgeDBService.getWeekEnd();
        cal2.add(Calendar.DATE, -7);
        Date end = cal2.getTime();
        Collection<Booking> bookings = bookingService.getBookingInRangeForUser(userLoginManager.getCurrentUser(), start, end);
        return workingHoursInRange(bookings);
    }

}
