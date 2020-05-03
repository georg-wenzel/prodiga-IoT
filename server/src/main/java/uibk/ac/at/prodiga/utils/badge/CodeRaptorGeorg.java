package uibk.ac.at.prodiga.utils.badge;

import uibk.ac.at.prodiga.model.BookingCategory;

import java.util.Collection;
import java.util.Optional;

public class CodeRaptorGeorg extends AbstractCategoryBadge {
    @Override
    public String getName() {
        return "Code Raptor Georg";
    }

    @Override
    public String getExplanation() {
        return "Most hours implementation";
    }

    @Override
    public Optional<BookingCategory> getCategory(Collection<BookingCategory> bookingCategories) {
        return bookingCategories.stream().filter(x -> x.getName().equals("Implementation")).findFirst();
    }
}
