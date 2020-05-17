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
import java.util.stream.Collectors;

@Component
@Scope("application")
public class BadgeDBService {

    private final BadgeDBRepository badgeDBRepository;
    private final BookingCategoryService bookingCategoryService;
    private final BookingService bookingService;
    private final LogInformationService logInformationService;

    private final List<Badge> availableBadges = new ArrayList<>();
    private Date lastWeekFrom;
    private Date lastWeekTo;

    public BadgeDBService(BadgeDBRepository badgeDBRepository, BookingCategoryService bookingCategoryService, BookingService bookingService, LogInformationService logInformationService) {
        this.badgeDBRepository = badgeDBRepository;
        this.bookingCategoryService = bookingCategoryService;
        this.bookingService = bookingService;
        this.logInformationService = logInformationService;

        registerBadges();
    }


    /**
     * Returns a collection of all badges for a user.
     * @return A collection of all badges for the given user.
     */
    @PreAuthorize("principal.username eq #user.username")
    public Collection<BadgeDB> getAllBadgesByUser(User user) {
        return Lists.newArrayList(badgeDBRepository.findByUser(user));
    }

    /**
     * Returns a collection of all badges for a user.
     * @return A collection of all badges for the given user.
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER')")
    public Collection<BadgeDB> getAllBadgesByDepartment(Department department) {
        return Lists.newArrayList(badgeDBRepository.findByDepartment(department));
    }

    /**
     * creates the Badges every Week on Sunday 23:59
     *
     */
    @Scheduled(cron = "0 59 23 * * SUN")
    public void createBadges(){
        for(Badge badge : availableBadges){
            logInformationService.logForCurrentUser("Started calculating user for badge " + badge.getName());
            User user = badge.calculateUser(bookingCategoryService.findAllCategories(), bookingService);

            if(user == null) {
                logInformationService.logForCurrentUser("No user found for badge " + badge.getName());
                continue;
            }

            logInformationService.logForCurrentUser("User " + user.getUsername() + " found for badge " + badge.getName());


            String name = badge.getName();
            String explanation = badge.getExplanation();
            BadgeDB badgeDB = new BadgeDB();
            badgeDB.setBadgeName(name);
            badgeDB.setExplanation(explanation);
            badgeDB.setUser(user);


            Date start = getWeekBeginning().getTime();
            Date end = getWeekEnd().getTime();
            badgeDB.setFromDate(start);
            badgeDB.setToDate(end);
            badgeDBRepository.save(badgeDB);

            lastWeekFrom = badgeDB.getFromDate();
            lastWeekTo = badgeDB.getToDate();
        }
    }

    public void registerBadges() {
        availableBadges.add(new Bugsimilian());
        availableBadges.add(new FrontendLaura());
        availableBadges.add(new CodeRaptorGeorg());
        availableBadges.add(new BusyBeeJamie());
        availableBadges.add(new EducatedGabbo());
        availableBadges.add(new TheSloth());
        availableBadges.add(new MostWorkingHours());
    }

    public void registerBadges(Badge badge) {
        availableBadges.add(badge);
    }

    /**
     * Returns the first badge with a matching name (unique identifier)
     * @param name The name of the badge
     * @return The first badge with a matching name, or null if none was found
     */
    public BadgeDB getFirstByBadgeName(String name)
    {
        return badgeDBRepository.findFirstByBadgeName(name);
    }

    public Collection<BadgeDB> getLastWeeksBadges(){
        Calendar cal = getWeekBeginning();
        cal.add(Calendar.DATE, -7);
        Date start = cal.getTime();

        Calendar cal2 = getWeekEnd();
        cal2.add(Calendar.DATE, -7);
        Date end = cal2.getTime();

        return this.badgeDBRepository.findBadgeDBSInRange(start, end);
    }

    public Collection<BadgeDB> getLastWeeksBadgesByUser(User user){
        Collection<BadgeDB> badgeDBS = getLastWeeksBadges();
        return badgeDBS.stream().filter(x -> x.getUser().equals(user)).collect(Collectors.toList());
    }

    public Calendar getWeekBeginning(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        return cal;
    }

    public Calendar getWeekEnd(){
        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.DAY_OF_WEEK, cal2.getFirstDayOfWeek());
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 0);
        cal2.add(Calendar.DATE, 6);

        return cal2;
    }
}
