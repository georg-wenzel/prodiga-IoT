package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.javatuples.Pair;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.DiceRepository;
import uibk.ac.at.prodiga.rest.dtos.DeviceType;
import uibk.ac.at.prodiga.rest.dtos.FeedAction;
import uibk.ac.at.prodiga.utils.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@Scope("application")
public class DiceService {

    private final DiceRepository diceRepository;
    private final ProdigaUserLoginManager prodigaUserLoginManager;
    private final LogInformationService logInformationService;
    private final DiceSideService diceSideService;
    private final BookingCategoryService bookingCategoryService;

    private final Map<String, DiceConfigurationWrapper> diceConfigurationWrapperDict = new HashMap<>();
    private final Map<UUID, Consumer<Pair<UUID, DiceConfigurationWrapper>>> onNewDiceSideCallBackDict = new HashMap<>();
    private final Map<Pair<UUID, String>, Instant> survivingTimerMap = new HashMap<>();

    public DiceService(DiceRepository diceRepository, ProdigaUserLoginManager prodigaUserLoginManager, DiceSideService diceSideService, LogInformationService logInformationService, BookingCategoryService bookingCategoryService) {
        this.diceRepository = diceRepository;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
        this.diceSideService = diceSideService;
        this.logInformationService = logInformationService;
        this.bookingCategoryService = bookingCategoryService;
    }

    /**
     * Returns all dices
     * @return A list with dices
     */
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('EMPLOYEE')") //NOSONAR
    public List<Dice> getAllDice() {
        User currentUser = prodigaUserLoginManager.getCurrentUser();
        if(currentUser.getRoles().contains(UserRole.ADMIN)) {
            return Lists.newArrayList(diceRepository.findAll());
        } else {
            Dice d = getDiceByUser(currentUser);
            if(d != null) {
                return Lists.newArrayList(d);
            } else {
                return new ArrayList<>();
            }
        }
    }

    /**
     * Gets the dice by the given id
     * @param diceId The dice id
     * @return The found dive
     * @throws ProdigaGeneralExpectedException If no dice could be found
     */
    public Dice loadDice(long diceId) throws ProdigaGeneralExpectedException {
        Dice d = diceRepository.findById(diceId).orElseThrow(()
                -> new ProdigaGeneralExpectedException("No dice with Id " + diceId + " found",
                MessageType.ERROR));

        checkAccessDiceAndThrow(d);

        return d;
    }

    /**
     * Returns the dice with the given internal id
     * @param internalId the internal id
     * @return The found dice
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public Dice getDiceByInternalIdWithAuth(String internalId) {
        return getDiceByInternalId(internalId);
    }

    /**
     * Returns the dice with the given internal id
     * @param internalId the internal id
     * @return The found dice
     */
    public Dice getDiceByInternalId(String internalId) {
        return diceRepository.findFirstByInternalId(internalId);
    }

    /**
     * Gets the dice assigned to the given user
     * @param u The user
     * @return The assigned dice
     */
    @PreAuthorize("hasAuthority('ADMIN') or principal.username eq #u.username")
    public Dice getDiceByUser(User u) {
        if(u == null) {
            return null;
        }
        return diceRepository.findFirstByUser(u);

    }

    /**
     * Returns all dice which are a signed to teh given raspi
     * @param raspi The raspi
     * @return A list with dices
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public List<Dice> getAllByRaspberryPi(RaspberryPi raspi) {
        return diceRepository.findAllByAssignedRaspberry(raspi);
    }

    /**
     * Returns all dices which are active and assigned to a user
     * @return A list of dices
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public List<Dice> getAllAvailableDices() {
        return getAllDice().stream()
                .filter(x -> x.isActive() && x.getUser() == null)
                .collect(Collectors.toList());
    }

    /**
     * Saves the given dice
     * @param dice The dice to save
     * @return The saved dice
     * @throws ProdigaGeneralExpectedException Either the dice does'nt have a assigned raspi, user or internalId
     */
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('EMPLOYEE')") //NOSONAR
    public Dice save(Dice dice) throws ProdigaGeneralExpectedException {
        checkAccessDiceAndThrow(dice);


        if(dice.isActive()) {

            if(StringUtils.isEmpty(dice.getInternalId())) {
                throw new ProdigaGeneralExpectedException("Dice needs a internal id", MessageType.ERROR);
            }

            // Here we check if the user already has a dice
            if(dice.getUser() != null) {
                Dice userDice = getDiceByUser(dice.getUser());
                // If the user already has a dice and the current dice is new we throw
                // ||
                // If the user already has a dice and the current dice is NOT the usersDice we also throw
                if(userDice != null && (dice.isNew() || !dice.getId().equals(userDice.getId()))) {
                    throw new ProdigaGeneralExpectedException("User " + dice.getUser().getUsername()
                            + " already has a assigned Dice", MessageType.ERROR);
                }
            }
        }

        if(dice.isNew()) {
            dice.setObjectCreatedDateTime(new Date());
            dice.setObjectCreatedUser(prodigaUserLoginManager.getCurrentUser());
        } else {
            dice.setObjectChangedUser(prodigaUserLoginManager.getCurrentUser());
            dice.setObjectChangedDateTime(new Date());
        }



        Dice result =  diceRepository.save(dice);

        logInformationService.logForCurrentUser("Dice " + dice.getInternalId() + " was saved");

        return result;
    }

    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public Dice createDice()
    {
        return new Dice();
    }

    /**
     * Deletes the dice.
     *
     * @param dice the dice to delete
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public void deleteDice(Dice dice) {
        diceRepository.delete(dice);
        logInformationService.logForCurrentUser("Dice " + dice.getInternalId() + " was deleted!");
    }


    /**
     * Returns the number of dice of users who are in the same team as the calling user, that have the corresponding Category set as one of their sides.
     * @param cat The category to look for
     * @return The number of dice in the given team, who have a side corresponding to the given category.
     */
    @PreAuthorize("hasAuthority('TEAMLEADER')")
    public int getDiceCountByCategoryAndTeam(BookingCategory cat)
    {
        return diceRepository.findDiceByUserTeamAndCategory(prodigaUserLoginManager.getCurrentUser().getAssignedTeam(), cat).size();
    }

    /**
     * If the dice is in configuration mode sets the current side to the given side
     * and notifies all subscribers about the change
     * @param internalId The dices internalId
     * @param side The current Side of the dice
     */
    public void onNewDiceSide(String internalId, int side) {
        //First check if the dice is in config mode
        if(diceConfigurationWrapperDict.containsKey(internalId)) {
            DiceConfigurationWrapper wrapper = diceConfigurationWrapperDict.get(internalId);
            wrapper.setCurrentSide(side);

            Pair<Integer, BookingCategory> value = wrapper.getCompletedSides().getOrDefault(side, null);

            if(value != null) {
                wrapper.setCurrentSideFriendlyName(value.getValue0());
            } else {
                wrapper.setCurrentSideFriendlyName(wrapper.getCurrentSideFriendlyName() + 1);
            }

            onNewDiceSideCallBackDict.forEach((key, v) -> v.accept(Pair.with(key, wrapper)));

            logInformationService.logForCurrentUser("Notified " + onNewDiceSideCallBackDict.size() + " listeners about new Dice Side " + side);
        }
    }

    /**
     * Returns whether the dice with given id is in configuration mode
     * @param internalId The dices internal id
     * @return Whether the dice is in configuration mode
     */
    public boolean diceInConfigurationMode(String internalId) {
        return diceConfigurationWrapperDict.containsKey(internalId);
    }

    /**
     * Can be called if your service wants to get notified when the dice side changes
     * @param id An identifier of your service
     * @param action The action which will be executed
     */
    public void registerNewSideCallback(UUID id, Consumer<Pair<UUID, DiceConfigurationWrapper>> action) {
        if(id == null) {
            return;
        }

        onNewDiceSideCallBackDict.put(id, action);

        logInformationService.logForCurrentUser("Registered " + id.toString() + " as new Dice Side Listener");
    }

    /**
     * Unregisters the service with the given id from the newSide Callback
     * @param id The services id
     */
    public void unregisterNewSideCallback(UUID id) {
        if(id == null) {
            return;
        }

        onNewDiceSideCallBackDict.remove(id);

        logInformationService.logForCurrentUser("Unregistered " + id.toString() + " from Dice Side Listener");
    }

    /**
     * Adds the given dice in configuration mode
     * @param d The dice to add
     */
    public DiceConfigurationWrapper addDiceToConfiguration(Dice d) {
        if(d == null) {
            return null;
        }

        DiceConfigurationWrapper wrapper = new DiceConfigurationWrapper();
        wrapper.setCurrentSide(-1);
        wrapper.setDice(d);
        wrapper.setCurrentSideFriendlyName(0);

        diceConfigurationWrapperDict.put(d.getInternalId(), wrapper);

        UUID feedId = FeedManager.getInstance().addToFeed(d.getInternalId(), DeviceType.CUBE, FeedAction.ENTER_CONFIG_MODE);

        wrapper.setFeedId(feedId);

        logInformationService.logForCurrentUser("Dice " + d.getInternalId() + " now in configuration mode");

        return wrapper;
    }

    /**
     * Completes the given Configuration for the given dice
     * @param d The dice
     * @throws ProdigaGeneralExpectedException When dice == null or not enough sides are configured
     */
    public void completeConfiguration(Dice d) throws ProdigaGeneralExpectedException {
        if(d == null) {
            return;
        }

        DiceConfigurationWrapper wrapper = diceConfigurationWrapperDict.getOrDefault(d.getInternalId(), null);

        if(wrapper == null) {
            return;
        }

        if(wrapper.getDice() == null) {
            throw new ProdigaGeneralExpectedException("Cannot complete configuration without dice", MessageType.ERROR);
        }

        if(wrapper.getCompletedSides() == null) {
            throw new ProdigaGeneralExpectedException("Cannot complete configuration without completed sides", MessageType.ERROR);
        }

        if(wrapper.getCompletedSides().values().stream().noneMatch(x -> x.getValue1().getId().equals(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID))) {
            BookingCategory bc = bookingCategoryService.findById(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID);

            throw new ProdigaGeneralExpectedException("At least one side must be configured with " + bc.getName(),
                    MessageType.ERROR);
        }

        wrapper.getCompletedSides().forEach((key, value) -> {
            diceSideService.onNewConfiguredDiceSide(key, value.getValue0(), value.getValue1(), wrapper.getDice());
        });

        diceConfigurationWrapperDict.remove(wrapper.getDice().getInternalId());

        FeedManager.getInstance().completeFeedItem(wrapper.getFeedId());

        FeedManager.getInstance().addToFeed(wrapper.getDice().getInternalId(), DeviceType.CUBE, FeedAction.LEAVE_CONFIG_MODE);

        logInformationService.logForCurrentUser("Dice " + d.getInternalId() + " left configuration mode");
    }

    /**
     * Ensures that the callback with the given id is still beeing executed
     * and the dice with the given internalId still in config mode
     * @param id The callbacks id
     * @param diceInternalId The dices internal Id
     */
    public void ensureListening(UUID id, String diceInternalId) {
        Pair<UUID, String> pair = new Pair<>(id, diceInternalId);

        Instant lastSurvivalTime = Instant.now();

        if(survivingTimerMap.containsKey(pair)) {
            survivingTimerMap.replace(pair, lastSurvivalTime);
        } else {
            survivingTimerMap.put(pair, lastSurvivalTime);
        }

        logInformationService.logForCurrentUser("Listener " + id.toString() + " still listening");
    }

    /**
     * Runs all 5 minutes and removes all not listening callbacks and dices
     */
    @Scheduled(fixedDelayString = "${removeNonListeningDelay}",
            initialDelayString = "${removeNonListeningDelay}")
    public void removeNonListening() {
        List<Map.Entry<Pair<UUID, String>, Instant>> notSurviving = survivingTimerMap.entrySet().stream()
                .filter(x -> x.getValue().isBefore(Instant.now().minus(Duration.ofMinutes(5))))
                .collect(Collectors.toList());

        logInformationService.logForCurrentUser("Found " + notSurviving.size() + " listeners which are not listening any more");

        for (Map.Entry<Pair<UUID, String>, Instant> entry: notSurviving) {
            survivingTimerMap.remove(entry.getKey());

            unregisterNewSideCallback(entry.getKey().getValue0());

            DiceConfigurationWrapper wrapper = diceConfigurationWrapperDict
                    .getOrDefault(entry.getKey().getValue1(), null);

            if(wrapper != null) {
                removeDiceInConfigMode(wrapper);
            }

            logInformationService.logForCurrentUser("Removed " + entry.getKey().getValue0().toString() + " from listeners");
        }
    }

    private void removeDiceInConfigMode(DiceConfigurationWrapper wrapper) {
        diceConfigurationWrapperDict.remove(wrapper.getDice().getInternalId());

        FeedManager.getInstance().completeFeedItem(wrapper.getFeedId());

        FeedManager.getInstance().addToFeed(wrapper.getDice().getInternalId(), DeviceType.CUBE, FeedAction.LEAVE_CONFIG_MODE);
    }

    private void checkAccessDiceAndThrow(Dice d) throws ProdigaGeneralExpectedException {
        User currentUser = prodigaUserLoginManager.getCurrentUser();

        if(d.getUser() == null && !currentUser.getRoles().contains(UserRole.ADMIN)) {
            throw new ProdigaGeneralExpectedException("Only admins can edit dices without users",
                    MessageType.ERROR);
        } else if(d.getUser() != null
                && !currentUser.getUsername().equals(d.getUser().getUsername())) {
            throw new ProdigaGeneralExpectedException("You cannot save someone else's dice",
                    MessageType.ERROR);
        }
    }
}
