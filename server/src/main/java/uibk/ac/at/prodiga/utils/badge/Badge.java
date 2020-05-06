package uibk.ac.at.prodiga.utils.badge;


import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.services.BookingService;

import java.util.Collection;

/**
 * Interface for calculating the User for a certain Badge.
 */
public interface Badge {
    User calculateUser(Collection<BookingCategory> bookingCategories, BookingService bookingService);
    String getName();
    String getExplanation();
}
