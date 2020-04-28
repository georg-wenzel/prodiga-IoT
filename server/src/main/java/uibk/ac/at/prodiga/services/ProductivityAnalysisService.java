package uibk.ac.at.prodiga.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
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

    public HashMap<BookingCategory, Long> getWeeklyStatisticForCurrentUser(){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){

            long millisec = 0;
            for(Booking booking : bookingService.getUsersBookingInRangeByCategoryForLastWeek(bookingCategory)) {
                millisec += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
            }
            long hours = millisec /(1000*60*60);
            hashMap.put(bookingCategory,hours);
        }
        return hashMap;
    }

    public HashMap<BookingCategory, Long> getLast24hourStatisticForCurrentUser(){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){

            long millisec = 0;
            for(Booking booking : bookingService.getUsersBookingInRangeByCategoryForLast24hours(bookingCategory)) {
                millisec += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
            }
            long hours = millisec /(1000*60*60);
            hashMap.put(bookingCategory,hours);
        }
        return hashMap;
    }

    public HashMap<BookingCategory, Long> getLastMonthsStatisticForCurrentUser(){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){

            long millisec = 0;
            for(Booking booking : bookingService.getUsersBookingInRangeByCategoryForLastMonth(bookingCategory)) {
                millisec += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
            }
            long hours = millisec /(1000*60*60);
            hashMap.put(bookingCategory,hours);
        }
        return hashMap;
    }

    public HashMap<BookingCategory, Long> getWeeklyStatisticForTeam(){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        User user = userLoginManager.getCurrentUser();
        Team myTeam = user.getAssignedTeam();
        if(user == userService.getTeamLeaderOf(myTeam)){
            for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
                long millisec = 0;
                for(User teamMember: userService.getUsersByTeam(myTeam)) {
                    for (Booking booking : bookingService.getUsersBookingInRangeByCategoryForLastWeek(bookingCategory)) {
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
        if(user == userService.getTeamLeaderOf(myTeam)){
            for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
                long millisec = 0;
                for(User teamMember: userService.getUsersByTeam(myTeam)) {
                    for (Booking booking : bookingService.getUsersBookingInRangeByCategoryForLast24hours(bookingCategory)) {
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
        if(user == userService.getTeamLeaderOf(myTeam)){
            for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
                long millisec = 0;
                for(User teamMember: userService.getUsersByTeam(myTeam)) {
                    for (Booking booking : bookingService.getUsersBookingInRangeByCategoryForLastMonth(bookingCategory)) {
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
