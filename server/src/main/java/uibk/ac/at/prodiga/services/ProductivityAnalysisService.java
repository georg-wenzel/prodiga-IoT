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
    private final UserService userService;


    public ProductivityAnalysisService(ProdigaUserLoginManager userLoginManager,BookingCategoryService bookingCategoryService, BookingService bookingService, UserService userService) {
        this.userLoginManager = userLoginManager;
        this.bookingCategoryService = bookingCategoryService;
        this.bookingService = bookingService;
        this.userService = userService;
    }

    public HashMap<BookingCategory,Long> getStatisicForCurrentUserByDay(int backstepDay){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        long hours;
        for(Booking booking: bookingService.getUsersBookingInRangeByDay(userLoginManager.getCurrentUser(),backstepDay)){
            hours = (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime())/(1000*60*60);
            if(hashMap.containsKey(booking.getBookingCategory())){
                long before = hashMap.get(booking.getBookingCategory());
                hashMap.put(booking.getBookingCategory(),before+hours);
            }
            else{
                hashMap.put(booking.getBookingCategory(),hours);
            }
        }
        return hashMap;
    }
    public HashMap<BookingCategory,Long> getStatisicForCurrentUserByWeek(int backstepWeek){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        long hours;
        for(Booking booking: bookingService.getUsersBookingInRangeByWeek(userLoginManager.getCurrentUser(),backstepWeek)){
            hours = (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime()) /(1000*60*60);;
            if(hashMap.containsKey(booking.getBookingCategory())){
                long before = hashMap.get(booking.getBookingCategory());
                hashMap.put(booking.getBookingCategory(),before+hours);
            }
            else{
                hashMap.put(booking.getBookingCategory(),hours);
            }
        }
        return hashMap;
    }
    public HashMap<BookingCategory,Long> getStatisicForCurrentUserByMonth(int backstepMonth){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        long hours;
        for(Booking booking: bookingService.getUsersBookingInRangeByMonth(userLoginManager.getCurrentUser(),backstepMonth)){
            hours = (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime()) /(1000*60*60);
            if(hashMap.containsKey(booking.getBookingCategory())){
                long before = hashMap.get(booking.getBookingCategory());
                hashMap.put(booking.getBookingCategory(),before+hours);
            }
            else{
                hashMap.put(booking.getBookingCategory(),hours);
            }
        }
        return hashMap;
    }

    public HashMap<BookingCategory,Long> getStatisicForTeamByWeek(int backstepWeek){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        User user = userLoginManager.getCurrentUser();
        Team myTeam = user.getAssignedTeam();
        long hours = 0;
        long before = 0;
        if(user.equals(userService.getTeamLeaderOf(myTeam))){
            for(User teamMember: userService.getUsersByTeam(myTeam)) {
                for (Booking booking : bookingService.getUsersBookingInRangeByWeek(teamMember, backstepWeek)) {
                    hours = (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime()) / (1000 * 60 * 60);
                    if (hashMap.containsKey(booking.getBookingCategory())) {
                        before = hashMap.get(booking.getBookingCategory());
                        hashMap.put(booking.getBookingCategory(), before + hours);
                    } else {
                        hashMap.put(booking.getBookingCategory(), hours);
                    }
                }
            }
        }
        return hashMap;
    }
    public HashMap<BookingCategory,Long> getStatisicForTeamByMonth(int backstepMonth){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        User user = userLoginManager.getCurrentUser();
        Team myTeam = user.getAssignedTeam();
        long hours = 0;
        long before = 0;
        if(user.equals(userService.getTeamLeaderOf(myTeam))){
            for(User teamMember: userService.getUsersByTeam(myTeam)) {
                for (Booking booking : bookingService.getUsersBookingInRangeByMonth(teamMember, backstepMonth)) {
                    hours = (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime()) / (1000 * 60 * 60);
                    if (hashMap.containsKey(booking.getBookingCategory())) {
                        before = hashMap.get(booking.getBookingCategory());
                        hashMap.put(booking.getBookingCategory(), before + hours);
                    } else {
                        hashMap.put(booking.getBookingCategory(), hours);
                    }
                }
            }
        }
        return hashMap;
    }

    public HashMap<BookingCategory,Long> getStatisicForDepartmenByMonth(int backstepMonth){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        User user = userLoginManager.getCurrentUser();
        Department myDepartment = user.getAssignedDepartment();
        long hours = 0;
        long before = 0;
        if(user.equals(userService.getDepartmentLeaderOf(myDepartment))){
            for(User departmentMember: userService.getUsersByDepartment(myDepartment)) {
                for (Booking booking : bookingService.getUsersBookingInRangeByMonth(departmentMember, backstepMonth)) {
                    hours = (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime()) / (1000 * 60 * 60);
                    if (hashMap.containsKey(booking.getBookingCategory())) {
                        before = hashMap.get(booking.getBookingCategory());
                        hashMap.put(booking.getBookingCategory(), before + hours);
                    } else {
                        hashMap.put(booking.getBookingCategory(), hours);
                    }
                }
            }
        }
        return hashMap;
    }
}
