package uibk.ac.at.prodiga.utils.badge;

import uibk.ac.at.prodiga.model.BookingCategory;

import java.util.Collection;
import java.util.Optional;

public class TheDiligentStudent extends AbstractCategoryBadge{
    @Override
    public String getName() {
        return "The diligent Student";
    }

    @Override
    public Optional<BookingCategory> getCategory(Collection<BookingCategory> bookingCategories) {
        return bookingCategories.stream().filter(x -> x.getName().equals("Education and Training")).findFirst();
    }
}
