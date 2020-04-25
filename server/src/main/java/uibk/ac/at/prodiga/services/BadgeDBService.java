package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.BadgeDBRepository;
import uibk.ac.at.prodiga.utils.badge.*;

import java.util.*;

@Component
@Scope("application")
public class BadgeDBService {

    private final BadgeDBRepository badgeDBRepository;
    private final BookingCategoryService bookingCategoryService;
    private final BookingService bookingService;

    private final List<Badge> availableBadges = new ArrayList<>();

    public BadgeDBService(BadgeDBRepository badgeDBRepository, BookingCategoryService bookingCategoryService, BookingService bookingService) {
        this.badgeDBRepository = badgeDBRepository;
        this.bookingCategoryService = bookingCategoryService;
        this.bookingService = bookingService;

        registerBadges();
    }


    /**
     * Returns a collection of all badges for a user.
     * @return A collection of all badges for the given user.
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public Collection<BadgeDB> getAllBadgesByUser(User user) {
        return Lists.newArrayList(badgeDBRepository.findByUser(user));
    }

    /**
     * creates the Badges every Week on Sunday 23:59
     *
     */
    @Scheduled(cron = "0 59 23 * * SUN")
    public void createBadges(){
        for(Badge badge : availableBadges){
            User user = badge.calculateUser(bookingCategoryService.findAllCategories(), bookingService);

            if(user == null) {
                continue;
            }

            String name = badge.getName();
            BadgeDB badgeDB = new BadgeDB();
            badgeDB.setBadgeName(name);
            badgeDB.setUser(user);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, cal.getActualMinimum(Calendar.DAY_OF_WEEK));
            Date start = cal.getTime();
            badgeDB.setFrom(start);
            badgeDB.setTo(new Date());
            badgeDBRepository.save(badgeDB);
        }
    }

    private void registerBadges() {
        availableBadges.add(new Bugsimilian());
        availableBadges.add(new CodeRaptor());
        availableBadges.add(new ConceptKing());
        availableBadges.add(new DocumentationDoctor());
        availableBadges.add(new FrontendLaura());
        availableBadges.add(new MeetingMaster());
        availableBadges.add(new TheDiligentStudent());
        availableBadges.add(new TheMostHelpfulOne());
        availableBadges.add(new TheSloth());
        availableBadges.add(new TheTester());
        availableBadges.add(new TheUltimateManager());
    }

    /**
     * Returns the first badge with a matching name (unique identifier)
     * @param name The name of the badge
     * @return The first badge with a matching name, or null if none was found
     */
    public BadgeDB getFirstByName(String name)
    {
        return badgeDBRepository.findFirstByBadgeName(name);
    }
}
