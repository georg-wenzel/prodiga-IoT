package uibk.ac.at.prodiga.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.User;

import java.util.HashMap;


@Component
@Scope("application")
public class ProductivityAnalysisService {
    private final BookingCategoryService bookingCategoryService;
    private User user;
    private final BookingService bookingService;

    public ProductivityAnalysisService(BookingCategoryService bookingCategoryService, BookingService bookingService) {
        this.bookingCategoryService = bookingCategoryService;
        this.bookingService = bookingService;
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

}
