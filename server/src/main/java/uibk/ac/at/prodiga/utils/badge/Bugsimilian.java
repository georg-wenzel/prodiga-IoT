package uibk.ac.at.prodiga.utils.badge;

import uibk.ac.at.prodiga.model.BookingCategory;

import java.util.*;

/**
 * Badge for person who debugged the most.
 */
public class Bugsimilian extends AbstractCategoryBadge {

    @Override
    public String getName() {
        return "Bugsimilian";
    }

    @Override
    public Optional<BookingCategory> getCategory(Collection<BookingCategory> bookingCategories) {
        return bookingCategories.stream().filter(x -> x.getName().equals("Debugging")).findFirst();
    }


}
