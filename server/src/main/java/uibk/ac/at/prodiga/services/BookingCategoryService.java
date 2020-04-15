package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.repositories.BookingCategoryRepository;
import uibk.ac.at.prodiga.repositories.BookingRepository;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Component
@Scope("application")
public class BookingCategoryService {

    private final BookingCategoryRepository bookingCategoryRepository;
    private final BookingRepository bookingRepository;
    private final ProdigaUserLoginManager prodigaUserLoginManager;

    public BookingCategoryService(BookingCategoryRepository bookingCategoryRepository, BookingRepository bookingRepository, ProdigaUserLoginManager prodigaUserLoginManager) {
        this.bookingCategoryRepository = bookingCategoryRepository;
        this.bookingRepository = bookingRepository;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
    }

    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('DEPARTMENTLEADER') || hasAuthority('TEAMLEADER')")
    public Collection<BookingCategory> findAllCategories() {
        return Lists.newArrayList(bookingCategoryRepository.findAll());
    }

    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('DEPARTMENTLEADER') || hasAuthority('TEAMLEADER')")
    public Collection<BookingCategory> findAllCategoriesByTeam(Team t) {
        if(t == null) {
            return new ArrayList<>();
        }

        return Lists.newArrayList(bookingCategoryRepository.findAllByTeamsContaining(t));
    }

    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('DEPARTMENTLEADER') || hasAuthority('TEAMLEADER')")
    public Collection<BookingCategory> findAllCategoriesNotUsedByTeam(Team t) {
        if(t == null) {
            return new ArrayList<>();
        }

        return Lists.newArrayList(bookingCategoryRepository.findAllByTeamsNotContaining(t));
    }

    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('DEPARTMENTLEADER') || hasAuthority('TEAMLEADER')")
    public BookingCategory save(BookingCategory cat) throws ProdigaGeneralExpectedException {
        if(cat == null) {
            return null;
        }

        if(StringUtils.isEmpty(cat.getName())) {
            throw new ProdigaGeneralExpectedException("Name may not be null", MessageType.ERROR);
        }

        if(cat.isNew()) {
            cat.setObjectCreatedDateTime(new Date());
            cat.setObjectCreatedUser(prodigaUserLoginManager.getCurrentUser());
        }

        cat.setObjectChangedDateTime(new Date());
        cat.setObjectChangedUser(prodigaUserLoginManager.getCurrentUser());

        return bookingCategoryRepository.save(cat);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(BookingCategory cat) throws ProdigaGeneralExpectedException {
        if(cat == null) {
            return;
        }

        if(cat.getTeams() != null && cat.getTeams().size() > 0) {
            throw new ProdigaGeneralExpectedException("Cannot delete category because it is used by at least one team", MessageType.ERROR);
        }


        if(bookingRepository.findAllByBookingCategory(cat).size() > 0) {
            throw new ProdigaGeneralExpectedException("Cannot delete category because it is used by at lease on booking", MessageType.ERROR);
        }

        bookingCategoryRepository.save(cat);
    }
}
