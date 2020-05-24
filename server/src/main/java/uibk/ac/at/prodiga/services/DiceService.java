package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.javatuples.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.DiceRepository;
import uibk.ac.at.prodiga.rest.dtos.DeviceType;
import uibk.ac.at.prodiga.rest.dtos.FeedAction;
import uibk.ac.at.prodiga.rest.dtos.PendingDiceDTO;
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
    private final RaspberryPiService raspberryPiService;
    private final BookingService bookingService;

    private List<Dice> activeDice;
    private final Map<String, DiceConfigurationWrapper> diceConfigurationWrapperDict = new HashMap<>();
    private final Map<UUID, Consumer<Pair<UUID, DiceConfigurationWrapper>>> onNewDiceSideCallBackDict = new HashMap<>();
    private final Map<Pair<UUID, String>, Instant> survivingTimerMap = new HashMap<>();
    private final List<Dice> pendingDices = Collections.synchronizedList(new ArrayList<>());

    public DiceService(DiceRepository diceRepository, ProdigaUserLoginManager prodigaUserLoginManager, DiceSideService diceSideService, LogInformationService logInformationService, BookingCategoryService bookingCategoryService, @Lazy RaspberryPiService raspberryPiService, @Lazy BookingService bookingService) {
        this.diceRepository = diceRepository;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
        this.diceSideService = diceSideService;
        this.logInformationService = logInformationService;
        this.bookingCategoryService = bookingCategoryService;
        this.raspberryPiService = raspberryPiService;
        this.bookingService = bookingService;
    }

    /**
     * Returns all dices
     * @return A list with dices
     */
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('EMPLOYEE')") //NOSONAR
    public List<Dice> getAllDice() {
        if(activeDice == null)
        {
            User currentUser = prodigaUserLoginManager.getCurrentUser();
            if(currentUser.getRoles().contains(UserRole.ADMIN)) {
                activeDice = Lists.newArrayList(diceRepository.findAll());
            } else {
                Dice d = getDiceByUser(currentUser);
                if(d != null) {
                    activeDice = Lists.newArrayList(d);
                } else {
                    activeDice = new ArrayList<>();
                }
            }
        }
        return activeDice;
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
     * Gets the dice with the given id
     * @param id The dices id
     * @return An Optional with the dice
     */
    public Optional<Dice> getDiceById(Long id) {
        return diceRepository.findById(id);
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
     * @throws ProdigaGeneralExpectedException Either the dice does'nt have a assigned raspi, user or internalId
     */
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('EMPLOYEE')") //NOSONAR
    public Dice save(Dice dice) throws ProdigaGeneralExpectedException {
        checkAccessDiceAndThrow(dice);

        Dice result = saveWithoutAuth(dice, prodigaUserLoginManager.getCurrentUser());

        tryDeletePendingDice(result.getInternalId());

        return result;
    }

    /**
     * Saves the given dice
     * @param dice The dice to save
     * @param user The user which saves the dice
     * @return The saved dice
     * @throws ProdigaGeneralExpectedException Either the dice does'nt have a assigned raspi, user or internalId
     */
    public Dice saveWithoutAuth(Dice dice, User user) throws ProdigaGeneralExpectedException {
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
            dice.setObjectCreatedUser(user);
        } else {

            // Here we check if the newly saved dice has a different user than previously
            // First get the dice
            Optional<Dice> oDice = getDiceById(dice.getId());

            if(oDice.isPresent()) {
                Dice dbDice = oDice.get();

                // If the dice needs to be cleared (see impl) we clear all bookings and dice sides
                if(diceNeedsToBeCleared(dice, dbDice)) {
                    clearDiceData(dice);
                }
            }

            dice.setObjectChangedUser(user);
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
     * Adds the given dices to the pending dices
     * @param diceRaspiMapping The dice internalIds
     */
    public void addDicesToPending(List<PendingDiceDTO> diceRaspiMapping) {
        for(PendingDiceDTO entry : diceRaspiMapping) {
            if(pendingDices.stream().anyMatch(x -> x.getInternalId().equals(entry.getDiceInternalId()))) {
                logInformationService.logForCurrentUser("Cannot add Dice " + entry.getDiceInternalId() + " to pending because it already exists");
                continue;
            }

            if(getDiceByInternalId(entry.getDiceInternalId()) != null) {
                logInformationService.logForCurrentUser("Dice " + entry.getDiceInternalId() + " already exists.");
                continue;
            }

            Optional<RaspberryPi> raspi = raspberryPiService.findByInternalId(entry.getRaspiInternalId());

            if(!raspi.isPresent()) {
                logInformationService.logForCurrentUser("Dice " + entry.getDiceInternalId() + " cannot be added because Raspberry Pi " + entry.getRaspiInternalId() + " does not exist");
                continue;
            }

            Dice d = new Dice();
            d.setInternalId(entry.getDiceInternalId());
            d.setAssignedRaspberry(raspi.get());
            pendingDices.add(d);
            logInformationService.logForCurrentUser("Added Dice " + entry.getDiceInternalId() + " to pending dices");
        }
    }

    /**
     * Returns all pending dices
     * @return A list with all pending dices
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public List<Dice> getPendingDices() {
        return new ArrayList<>(pendingDices);
    }

    /**
     * Deletes the dice.
     *
     * @param dice the dice to delete
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public void deleteDice(Dice dice) throws ProdigaGeneralExpectedException {
        if(dice == null) {
            return;
        }

        clearDiceData(dice);

        diceRepository.delete(dice);
        activeDice = null;
        logInformationService.logForCurrentUser("Dice " + dice.getInternalId() + " was deleted!");
    }

    /**
     * Gets the battery status for the given user
     * @param u The user
     * @return The battery status
     */
    public String getDiceBatteryStatusForUser(User u) {
        if(u == null) {
            return null;
        }

        Dice d = getDiceByUser(u);

        if(d == null) {
            return null;
        }

        if(d.getLastBatteryStatus() == null){
            return "n/a";
        }

        return d.getLastBatteryStatus().toString() + "%";
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

            Integer value = wrapper.getVisitedSites().getOrDefault(side, null);

            if(value != null) {
                wrapper.setCurrentSideFriendlyName(value);
            } else {
                int newFriendlySide = wrapper.getCurrentSideFriendlyName() + 1;
                wrapper.setCurrentSideFriendlyName(newFriendlySide);
                wrapper.getVisitedSites().put(side, newFriendlySide);
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
     * @param internalId The dices internal id
     */
    public void unregisterNewSideCallback(UUID id, String internalId) {
        if(id == null) {
            return;
        }

        // Remove dice from config mode, unregister callback and send message to client
        onNewDiceSideCallBackDict.remove(id);
        diceConfigurationWrapperDict.remove(internalId);
        FeedManager.getInstance().addToFeed(internalId, DeviceType.CUBE, FeedAction.LEAVE_CONFIG_MODE);

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

        diceSideService.clearSidesForDice(d);

        wrapper.getCompletedSides().forEach((key, value) -> {
            diceSideService.onNewConfiguredDiceSide(key, value.getValue0(), value.getValue1(), wrapper.getDice());
        });

        removeDiceInConfigMode(wrapper);

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

            unregisterNewSideCallback(entry.getKey().getValue0(), entry.getKey().getValue1());

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
                && !currentUser.getUsername().equals(d.getUser().getUsername()) && !currentUser.getRoles().contains(UserRole.ADMIN)) {
            throw new ProdigaGeneralExpectedException("You cannot save someone else's dice",
                    MessageType.ERROR);
        }
    }

    private void tryDeletePendingDice(String internalId){
        pendingDices.stream()
                .filter(x -> x.getInternalId().equals(internalId))
                .findFirst().ifPresent(diceInList -> {
                    pendingDices.remove(diceInList);
                    logInformationService.logForCurrentUser("Removed Dice " + diceInList.getInternalId() + " from pending dices");
        });
    }

    /**
     * Removes all data associated with the given dice - currently bookings and dice Sides
     * @param d The dice
     * @throws ProdigaGeneralExpectedException Can occur when deleting bookings
     */
    private void clearDiceData(Dice d) throws ProdigaGeneralExpectedException {
        bookingService.deleteBookingsForDice(d);
        diceSideService.deleteForDice(d);
    }

    /**
     * Returns whether the given dice and the dice in the db need to be cleared
     * @param dice The dice to save
     * @param dbDice The dice in the db
     * @return Whether the dice needs to be cleared
     */
    private boolean diceNeedsToBeCleared(Dice dice, Dice dbDice) {
        // If the current dice and the dice in the DB both have a user and the user is different
        // we delete all data from the dice
        return (
                dice.getUser() != null && dbDice.getUser() != null &&
                        !dbDice.getUser().getUsername().equals(dice.getUser().getUsername())
                )
                ||
                // if the current dice does not have an user and the dice in the DB has a user we know
                // the user is unassigned - clear all data
                (
                    dice.getUser() == null && dbDice.getUser() != null
                );
    }
}
