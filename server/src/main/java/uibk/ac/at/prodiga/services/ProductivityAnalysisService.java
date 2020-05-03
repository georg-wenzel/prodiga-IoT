package uibk.ac.at.prodiga.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.HashMap;


@Component
@Scope("application")
public class ProductivityAnalysisService {
    private final BookingCategoryService bookingCategoryService;
    private final ProdigaUserLoginManager userLoginManager;
    private final BookingService bookingService;
    private final TeamService teamService;
    private final UserService userService;


    public ProductivityAnalysisService(ProdigaUserLoginManager userLoginManager,BookingCategoryService bookingCategoryService, BookingService bookingService, TeamService teamService, UserService userService) {
        this.userLoginManager = userLoginManager;
        this.bookingCategoryService = bookingCategoryService;
        this.bookingService = bookingService;
        this.teamService = teamService;
        this.userService = userService;
    }

    /**
     * Returns a hash map of the total hours of last weeks bookings for each category for the logged in user
     * where the booking categories are the key and the hours are the value
     * @return the total hours of last weeks bookings for each category for the logged in user
     */
    public HashMap<BookingCategory, Long> getWeeklyStatisticForCurrentUser(){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
            long millisec = 0;
            for(Booking booking : bookingService.getUsersBookingInRangeByCategoryForLastWeek(userLoginManager.getCurrentUser(),bookingCategory)) {
                millisec += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
            }
            long hours = millisec /(1000*60*60);
            hashMap.put(bookingCategory,hours);
        }
        return hashMap;
    }

    /**
     * Returns a hash map of the total hours of the bookings done in the last 24 hours by
     * category for the logged in user, where the booking categories are the key
     * and the hours are the value
     * @return the total hours of the bookings done in the last 24 hours by category for the logged in user
     */
    public HashMap<BookingCategory, Long> getLast24hourStatisticForCurrentUser(){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
            long millisec = 0;
            for(Booking booking : bookingService.getUsersBookingInRangeByCategoryForLast24hours(userLoginManager.getCurrentUser(),bookingCategory)) {
                millisec += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
            }
            long hours = millisec /(1000*60*60);
            hashMap.put(bookingCategory,hours);
        }
        return hashMap;
    }

    /**
     * Returns a hash map of the total hours of the bookings done in the last month by
     * category for the logged in user, where the booking categories are the key
     * and the hours are the value
     * @return the total hours of the bookings done in the last month by category for the logged in user
     */
    public HashMap<BookingCategory, Long> getLastMonthsStatisticForCurrentUser(){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
            long millisec = 0;
            for(Booking booking : bookingService.getUsersBookingInRangeByCategoryForLastMonth(userLoginManager.getCurrentUser(),bookingCategory)) {
                millisec += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
            }
            long hours = millisec /(1000*60*60);
            hashMap.put(bookingCategory,hours);
        }
        return hashMap;
    }

    /**
     * if the logged in user is a team leader the methode returns a hash map of the total hours
     * of the bookings done by the team members in the last week by
     * category, where the booking categories are the key
     * and the hours are the value
     * @return the total hours of the bookings done in the last week by category for the logged in user
     */
    public HashMap<BookingCategory, Long> getWeeklyStatisticForTeam(){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        User user = userLoginManager.getCurrentUser();
        Team myTeam = user.getAssignedTeam();
        if(user.equals(userService.getTeamLeaderOf(myTeam))){
            for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
                long millisec = 0;
                for(User teamMember: userService.getUsersByTeam(myTeam)) {
                    for (Booking booking : bookingService.getUsersBookingInRangeByCategoryForLastWeek(teamMember,bookingCategory)) {
                        millisec += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
                    }
                    long hours = millisec / (1000 * 60 * 60);
                    hashMap.put(bookingCategory, hours);
                }
            }
        }
        return hashMap;
    }

    public HashMap<BookingCategory, Long> getLast24hourStatisticForTeam(){
        User user = userLoginManager.getCurrentUser();
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        Team myTeam = user.getAssignedTeam();
        if(user.equals(userService.getTeamLeaderOf(myTeam))){
            for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
                long millisec = 0;
                for(User teamMember: userService.getUsersByTeam(myTeam)) {
                    for (Booking booking : bookingService.getUsersBookingInRangeByCategoryForLast24hours(teamMember,bookingCategory)) {
                        millisec += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
                    }
                    long hours = millisec / (1000 * 60 * 60);
                    hashMap.put(bookingCategory, hours);
                }
            }
        }
        return hashMap;
    }

    public HashMap<BookingCategory, Long> getLastMonthsStatisticForTeam(){
        User user = userLoginManager.getCurrentUser();
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        Team myTeam = user.getAssignedTeam();
        if(user.equals(userService.getTeamLeaderOf(myTeam))){
            for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
                long millisec = 0;
                for(User teamMember: userService.getUsersByTeam(myTeam)) {
                    for (Booking booking : bookingService.getUsersBookingInRangeByCategoryForLastMonth(teamMember,bookingCategory)) {
                        millisec += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
                    }
                    long hours = millisec / (1000 * 60 * 60);
                    hashMap.put(bookingCategory, hours);
                }
            }
        }
        return hashMap;
    }

    public HashMap<BookingCategory, Long> getWeeklyStatisticForDepartment(){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        User user = userLoginManager.getCurrentUser();
        Department myDepartment = user.getAssignedDepartment();
        if(user.equals(userService.getDepartmentLeaderOf(myDepartment))){
            for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
                long millisec = 0;
                for(User departmentMember: userService.getUsersByDepartment(myDepartment)) {
                    for (Booking booking : bookingService.getUsersBookingInRangeByCategoryForLastWeek(departmentMember,bookingCategory)) {
                        millisec += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
                    }
                    long hours = millisec / (1000 * 60 * 60);
                    hashMap.put(bookingCategory, hours);
                }
            }
        }
        return hashMap;
    }

    public HashMap<BookingCategory, Long> getLast24hourStatisticForDepartment(){
        User user = userLoginManager.getCurrentUser();
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        Department myDepartment = user.getAssignedDepartment();
        if(user.equals(userService.getDepartmentLeaderOf(myDepartment))){
            for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
                long millisec = 0;
                for(User departmentMember: userService.getUsersByDepartment(myDepartment)) {
                    for (Booking booking : bookingService.getUsersBookingInRangeByCategoryForLast24hours(departmentMember,bookingCategory)) {
                        millisec += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
                    }
                    long hours = millisec / (1000 * 60 * 60);
                    hashMap.put(bookingCategory, hours);
                }
            }
        }
        return hashMap;
    }
    public HashMap<BookingCategory, Long> getLastMonthsStatisticForDepartment(){
        User user = userLoginManager.getCurrentUser();
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        Department myDepartment = user.getAssignedDepartment();
        if(user.equals(userService.getDepartmentLeaderOf(myDepartment))){
            for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
                long millisec = 0;
                for(User departmentMember: userService.getUsersByDepartment(myDepartment)) {
                    for (Booking booking : bookingService.getUsersBookingInRangeByCategoryForLastMonth(departmentMember,bookingCategory)) {
                        millisec += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
                    }
                    long hours = millisec / (1000 * 60 * 60);
                    hashMap.put(bookingCategory, hours);
                }
            }
        }
        return hashMap;
    }



}
