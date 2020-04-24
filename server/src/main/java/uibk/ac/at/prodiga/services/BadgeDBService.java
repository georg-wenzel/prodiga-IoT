package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.BadgeDBRepository;
import uibk.ac.at.prodiga.utils.badge.Badge;
import uibk.ac.at.prodiga.utils.badge.Bugsimilian;

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
    public Collection<BadgeDB> getAllBadgesByUser(User user) {
        return Lists.newArrayList(badgeDBRepository.findByUser(user));
    }

    @Scheduled(cron = "0 0 12 ? * L *")
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
            badgeDB.setDate(new Date());
            badgeDBRepository.save(badgeDB);
        }
    }

    private void registerBadges() {
        availableBadges.add(new Bugsimilian());
    }
}
