package uibk.ac.at.prodiga.utils.badge;

import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.services.BookingService;

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

        Optional<Map.Entry<User, Long>> result =  bookings.stream()
                .collect(Collectors.groupingBy(x -> x.getDice().getUser(), Collectors.counting()))
                .entrySet().stream().max(Comparator.comparingLong(Map.Entry::getValue));

        if(!result.isPresent()){
            return null;
        }

        return result.get().getKey();
    }

    @Override
    public abstract String getName();

    public abstract Optional<BookingCategory> getCategory(Collection<BookingCategory> bookingCategories);
}
