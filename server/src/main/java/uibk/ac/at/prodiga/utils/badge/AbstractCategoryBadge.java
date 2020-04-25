package uibk.ac.at.prodiga.utils.badge;

import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.services.BookingService;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract batch class for categories
 */

public abstract class AbstractCategoryBadge implements Badge {
    @Override
    public final User calculateUser(Collection<BookingCategory> bookingCategories, BookingService bookingService) {
        Optional<BookingCategory> category = getCategory(bookingCategories);

        if(!category.isPresent()) {
            return null;
        }


        Collection<Booking> bookings = bookingService.getBookingInRangeByCategoryForLastWeek(category.get());
        HashMap<User, Long> hashMap = new HashMap<>();
        for(Booking b : bookings){
            if(hashMap.containsKey(b.getDice().getUser())){
                hashMap.put(b.getDice().getUser(), hashMap.get(b.getDice().getUser()) + b.getActivityEndDate().getTime()-b.getActivityStartDate().getTime());
            }
            else{
                hashMap.put(b.getDice().getUser(), b.getActivityEndDate().getTime()-b.getActivityStartDate().getTime());
            }
        }

        User userToReturn = Collections.max(hashMap.entrySet(), Comparator.comparingLong(Map.Entry::getValue)).getKey();

        return userToReturn;
    }

    @Override
    public abstract String getName();

    public abstract Optional<BookingCategory> getCategory(Collection<BookingCategory> bookingCategories);
}
