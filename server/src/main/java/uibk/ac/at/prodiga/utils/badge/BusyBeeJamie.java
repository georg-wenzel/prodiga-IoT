package uibk.ac.at.prodiga.utils.badge;

import uibk.ac.at.prodiga.model.BookingCategory;

import java.util.Collection;
import java.util.Optional;

public class BusyBeeJamie extends AbstractCategoryBadge {
    @Override
    public String getName() {
        return "Busy Bee Jamie";
    }

    @Override
    public String getExplanation() {
        return "Most hours project management";
    }

    @Override
    public Optional<BookingCategory> getCategory(Collection<BookingCategory> bookingCategories) {
        return bookingCategories.stream().filter(x -> x.getName().equals("Project Management")).findFirst();
    }
}
