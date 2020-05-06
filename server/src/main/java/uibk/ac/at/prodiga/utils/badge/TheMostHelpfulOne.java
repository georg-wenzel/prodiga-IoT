package uibk.ac.at.prodiga.utils.badge;

import uibk.ac.at.prodiga.model.BookingCategory;

import java.util.Collection;
import java.util.Optional;

public class TheMostHelpfulOne extends AbstractCategoryBadge{
    @Override
    public String getName() {
        return "The most helpful one";
    }

    @Override
    public String getExplanation() {
        return "Most hours customer support";
    }

    @Override
    public Optional<BookingCategory> getCategory(Collection<BookingCategory> bookingCategories) {
        return bookingCategories.stream().filter(x -> x.getName().equals("Customer Support")).findFirst();
    }
}
