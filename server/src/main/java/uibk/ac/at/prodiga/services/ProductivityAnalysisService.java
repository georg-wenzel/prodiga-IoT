package uibk.ac.at.prodiga.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import sun.jvm.hotspot.utilities.Interval;
import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.User;

import java.util.HashMap;


@Component
@Scope("application")
public class ProductivityAnalysisService {
    BookingCategoryService bookingCategoryService;
    User user;
    BookingService bookingService;

    public HashMap<BookingCategory, Long> getWeeklyStatisticForCurrentUser(){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        for(BookingCategory bookingCategory : bookingCategoryService.findAllCategories()){
            long time = 0;
             for(Booking booking : bookingService.getUsersBookingInRangeByCategoryForLastWeek(bookingCategory)) {
                 time += booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime();
             }
             hashMap.put(bookingCategory,time);
        }
        return hashMap;
    }

}
