package uibk.ac.at.prodiga.utils.badge;

import uibk.ac.at.prodiga.model.BookingCategory;

import java.util.Collection;
import java.util.Optional;

public class DocumentationDoctor extends AbstractCategoryBadge {
    @Override
    public String getName() {
        return "Documentation Doctor";
    }

    @Override
    public String getExplanation() {
        return "Most hours documentation";
    }

    @Override
    public Optional<BookingCategory> getCategory(Collection<BookingCategory> bookingCategories) {
        return bookingCategories.stream().filter(x -> x.getName().equals("Documentation")).findFirst();
    }
}
