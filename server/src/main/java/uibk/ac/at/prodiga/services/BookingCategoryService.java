package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.BookingCategoryRepository;
import uibk.ac.at.prodiga.repositories.BookingRepository;
import uibk.ac.at.prodiga.utils.Constants;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.*;

@Component
@Scope("application")
public class BookingCategoryService
{

    private final BookingCategoryRepository bookingCategoryRepository;
    private final BookingRepository bookingRepository;
    private final ProdigaUserLoginManager prodigaUserLoginManager;
    private final LogInformationService logInformationService;

    public BookingCategoryService(BookingCategoryRepository bookingCategoryRepository, BookingRepository bookingRepository, ProdigaUserLoginManager prodigaUserLoginManager, LogInformationService logInformationService)
    {
        this.bookingCategoryRepository = bookingCategoryRepository;
        this.bookingRepository = bookingRepository;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
        this.logInformationService = logInformationService;
    }

    @PreAuthorize("hasAuthority('EMPLOYEE')") //NOSONAR
    public BookingCategory findById(long id)
    {
        Optional<BookingCategory> cat = bookingCategoryRepository.findById(id);
        return cat.orElse(null);
    }

    /**
     * Returns all saved booking categories
     * @return All booking categories
     */
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('DEPARTMENTLEADER') || hasAuthority('TEAMLEADER') || hasAuthority('EMPLOYEE')") //NOSONAR
    public Collection<BookingCategory> findAllCategories()
    {
        return Lists.newArrayList(bookingCategoryRepository.findAllExcept(Constants.VACATION_BOOKING_ID));
    }

    /**
     * Returns all booking categories used by a team
     * @param t The team
     * @return All booking categories used by that team
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public Collection<BookingCategory> findAllCategoriesByTeam(Team t)
    {
        if(t == null) {
            return new ArrayList<>();
        }
        List<BookingCategory> teamCategories = Lists.newArrayList(bookingCategoryRepository.findAllByTeamExcept(t, Constants.VACATION_BOOKING_ID));
        bookingCategoryRepository.findById(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID).ifPresent(teamCategories::add);
        return teamCategories;
    }

    /**
     * Returns all booking categories of the user's team
     * @return All booking categories of the team the user is in. This should always exist, since the user calling the method is teamleader.
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')") //NOSONAR
    public Collection<BookingCategory> findAllCategoriesByTeam()
    {
        List<BookingCategory> teamCategories = Lists.newArrayList(bookingCategoryRepository.findAllByTeamExcept(prodigaUserLoginManager.getCurrentUser().getAssignedTeam(), Constants.VACATION_BOOKING_ID));
        bookingCategoryRepository.findById(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID).ifPresent(teamCategories::add);
        return teamCategories;
    }

    /**
     * Returns all booking categories not used by a team
     * @param t The team
     * @return All booking categories not used by that team
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public Collection<BookingCategory> findAllCategoriesNotUsedByTeam(Team t)
    {
        if(t == null) {
            return new ArrayList<>();
        }

        return Lists.newArrayList(bookingCategoryRepository.findAllWithoutTeamExcept(t, Constants.VACATION_BOOKING_ID));
    }

    /**
     * Returns all booking categories not used by the team the calling user is in
     * @return All booking categories not used by that team.
     */
    @PreAuthorize("hasAuthority('TEAMLEADER')") //NOSONAR
    public Collection<BookingCategory> findAllCategoriesNotUsedByTeam()
    {
        return Lists.newArrayList(bookingCategoryRepository.findAllByTeamExcept(prodigaUserLoginManager.getCurrentUser().getAssignedTeam(), Constants.VACATION_BOOKING_ID));
    }

    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public BookingCategory save(BookingCategory cat) throws ProdigaGeneralExpectedException
    {
        if(cat == null) {
            return null;
        }

        if(StringUtils.isEmpty(cat.getName())) {
            throw new ProdigaGeneralExpectedException("Name may not be null", MessageType.ERROR);
        }

        if(cat.getName().length() < 2 || cat.getName().length() > 20) {
            throw new ProdigaGeneralExpectedException("Name must be between 2 and 20 characters.", MessageType.ERROR);
        }

        if(cat.getId() != null && cat.getId().equals(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID)) throw new ProdigaGeneralExpectedException("The category " + cat.getName() + " is a mandatory category and may not be edited.", MessageType.ERROR);

        if(cat.isNew()) {
            cat.setObjectCreatedDateTime(new Date());
            cat.setObjectCreatedUser(prodigaUserLoginManager.getCurrentUser());
        } else {
            cat.setObjectChangedDateTime(new Date());
            cat.setObjectChangedUser(prodigaUserLoginManager.getCurrentUser());
        }

        BookingCategory result = bookingCategoryRepository.save(cat);

        logInformationService.logForCurrentUser("Saved Booking Category " + result.getName());

        return result;
    }

    /**
     * Deletes given booking category
     * @param cat The booking category to delete
     * @throws ProdigaGeneralExpectedException Is thrown when category can't be deleted, i.e. because it is still in use or was in use in the past.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(BookingCategory cat) throws ProdigaGeneralExpectedException
    {
        if(cat == null) {
            return;
        }

        if(cat.getTeams() != null && cat.getTeams().size() > 0) {
            throw new ProdigaGeneralExpectedException("Cannot delete category because it is used by at least one team", MessageType.ERROR);
        }

        if(bookingRepository.findAllByBookingCategory(cat).size() > 0) {
            throw new ProdigaGeneralExpectedException("Cannot delete category because it is used by at least one booking", MessageType.ERROR);
        }

        if(cat.getId() != null && cat.getId().equals(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID))
        {
            throw new ProdigaGeneralExpectedException("Cannot delete mandatory category.", MessageType.ERROR);
        }

        if(cat.getId() != null & cat.getId().equals(Constants.VACATION_BOOKING_ID))
        {
            throw new RuntimeException("Cannot delete vacation category.");
        }

        bookingCategoryRepository.delete(cat);

        logInformationService.logForCurrentUser("Deleted Booking Category " + cat.getName());
    }

    @PreAuthorize("hasAuthority('TEAMLEADER')")
    public void allowForTeam(BookingCategory cat) throws ProdigaGeneralExpectedException
    {
        if(cat == null || cat.getId() == null) return;
        User teamleader = prodigaUserLoginManager.getCurrentUser();
        BookingCategory db_cat = bookingCategoryRepository.findById(cat.getId()).orElse(null);

        if(db_cat == null)
            throw new ProdigaGeneralExpectedException("Category was not found. Please reload the page to update categories.", MessageType.ERROR);

        if(!db_cat.getName().equals(cat.getName()))
            throw new ProdigaGeneralExpectedException("Category name has changed. Consider reloading the page before making changes.", MessageType.WARNING);

        Set<Team> teams = db_cat.getTeams();
        teams.add(teamleader.getAssignedTeam());
        db_cat.setTeams(teams);
        bookingCategoryRepository.save(db_cat);
    }

    @PreAuthorize("hasAuthority('TEAMLEADER')")
    public void disallowForTeam(BookingCategory cat) throws ProdigaGeneralExpectedException
    {
        if(cat == null || cat.getId() == null) return;
        if(cat.getId().equals(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID)) throw new ProdigaGeneralExpectedException("The category " + cat.getName() + " is a mandatory category and may not be unassigned from a team.", MessageType.ERROR);
        User teamleader = prodigaUserLoginManager.getCurrentUser();
        BookingCategory db_cat = bookingCategoryRepository.findById(cat.getId()).orElse(null);

        if(db_cat == null)
            throw new ProdigaGeneralExpectedException("Category was not found. Please reload the page to update categories.", MessageType.ERROR);

        if(!db_cat.getName().equals(cat.getName()))
            throw new ProdigaGeneralExpectedException("Category name has changed. Consider reloading the page before making changes.", MessageType.WARNING);

        Set<Team> teams = db_cat.getTeams();
        teams.remove(teamleader.getAssignedTeam());
        db_cat.setTeams(teams);
        bookingCategoryRepository.save(db_cat);
    }
}
