package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.Room;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
import uibk.ac.at.prodiga.utils.Constants;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.*;

@Component
@Scope("application")
public class RaspberryPiService {

    private final RaspberryPiRepository raspberryPiRepository;
    private final ProdigaUserLoginManager prodigaUserLoginManager;
    private final LogInformationService logInformationService;
    private final DiceService diceService;

    private final List<RaspberryPi> pendingRaspberryPis = Collections.synchronizedList(new ArrayList<>());

    public RaspberryPiService(RaspberryPiRepository raspberryPiRepository,
                              ProdigaUserLoginManager prodigaUserLoginManager,
                              LogInformationService logInformationService,
                              DiceService diceService) {
        this.raspberryPiRepository = raspberryPiRepository;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
        this.logInformationService = logInformationService;
        this.diceService = diceService;
    }

    /**
     * Finds the raspberry pi by the given internal id
     * @param internalId The internal id
     * @return An Optional with the found raspberry
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public Optional<RaspberryPi> findByInternalIdWithAuth(String internalId) {
        return findByInternalId(internalId);
    }

    /**
     * Finds the given raspberry and throws a exception when it could not be found
     * @param internalId The given internal id
     * @return The raspberry
     * @throws Exception Exception which is thrown when the raspberry could not be found
     */
    public RaspberryPi findByInternalIdWithAuthAndThrow(String internalId) throws Exception {
        return findByInternalIdWithAuth(internalId)
                .orElseThrow(() -> new ProdigaGeneralExpectedException(
                        "RaspberryPi with internal id " + internalId + " not " +
                                "found", MessageType.ERROR));
    }

    /**
     * Finds the raspberry pi by the given internal id
     * @param internalId The internal id
     * @return An Optional with the found raspberry
     */
    public Optional<RaspberryPi> findByInternalId(String internalId) {
        return raspberryPiRepository.findFirstByInternalId(internalId);
    }

    /**
     * Finds the given raspberry and throws a exception when it could not be found
     * @param internalId The given internal id
     * @return The raspberry
     * @throws Exception Exception which is thrown when the raspberry could not be found
     */
    public RaspberryPi findByInternalIdAndThrow(String internalId) throws Exception {
        return findByInternalId(internalId)
                .orElseThrow(() -> new ProdigaGeneralExpectedException(
                        "RaspberryPi with internal id " + internalId + " not " +
                                "found", MessageType.ERROR));
    }

    public RaspberryPi findById(Long raspId) throws Exception {
        return raspberryPiRepository.findById(raspId)
                .orElseThrow(
                        () -> new ProdigaGeneralExpectedException("Could not find Raspberry Pi wiht id " + raspId, MessageType.ERROR));
    }

    /**
     * Returns all raspberry pis which are not configured
     * @return A list of raspberry pis
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public List<RaspberryPi> getAllPendingRaspberryPis() {
        // Make a copy here, so the reference is not thread safe any more
        return new ArrayList<>(pendingRaspberryPis);
    }

    /**
     * Retturns a list of all raspberry pis which are configured
     * @return A list of raspberry pis
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public List<RaspberryPi> getAllConfiguredRaspberryPis() {
        return Lists.newArrayList(raspberryPiRepository.findAll());
    }

    /**
     * Returns the number of configured raspberry Pis
     * @return The number of configured raspberry pis
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')") //NOSONAR
    public int getNumConfiguredRaspberryPis() {
        return Lists.newArrayList(raspberryPiRepository.findAll()).size();
    }


    /**
     * Adds a new raspberry to the pending list
     * @param internalId The raspberry pis internal ID
     */
    public boolean tryAddPendingRaspberry(String internalId) {
        if(pendingRaspberryPis.stream().anyMatch(x -> x.getInternalId().equals(internalId))) {
            logInformationService.logForCurrentUser("Raspberry Pi cannot be added to pending because there already exits one with the same InternalID");
            return false;
        }

        if(findByInternalId(internalId).isPresent()){
            logInformationService.logForCurrentUser("Raspberry Pi cannot be added to pending because it already exists");
            return false;
        }

        RaspberryPi raspi = new RaspberryPi();
        raspi.setInternalId(internalId);
        pendingRaspberryPis.add(raspi);
        logInformationService.logForCurrentUser("Raspberry Pi with internal ID " + internalId +
                " added to pending Raspberrys");

        return true;
    }



    /**
     * Saves the given raspberry pi
     * @param raspi The raspberry pi to save
     * @return The saved raspberry pi
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public RaspberryPi save(RaspberryPi raspi) throws Exception {
        if(raspi == null) {
            return null;
        }

        if(raspi.isNew()) {
            if(raspberryPiRepository.findFirstByInternalId(raspi.getInternalId()).isPresent()) {
                throw new ProdigaGeneralExpectedException("Raspberry Pi with internal ID "
                        + raspi.getInternalId() + " already exists.", MessageType.ERROR);
            }
        }

        // First check if there is a room
        if(raspi.getAssignedRoom() == null) {
            throw new ProdigaGeneralExpectedException("Cannot save Raspberry Pi without room!", MessageType.ERROR);
        }

        // Next check if the password is set
        if(StringUtils.isEmpty(raspi.getPassword())) {
            throw new ProdigaGeneralExpectedException("Cannot save Raspberry Pi with empty password!", MessageType.ERROR);
        }

        // Check if internal ID is set
        if(StringUtils.isEmpty(raspi.getInternalId())) {
            throw new ProdigaGeneralExpectedException("Cannot save Raspberry Pi with empty Internal ID", MessageType.ERROR);
        }

        tryDeletePendingRaspberry(raspi);

        if(raspi.isNew()) {
            // If the raspi is new we have to hash the password here
            raspi.setPassword(Constants.PASSWORD_ENCODER.encode(raspi.getPassword()));

            raspi.setObjectCreatedDateTime(new Date());
            raspi.setObjectCreatedUser(prodigaUserLoginManager.getCurrentUser());
        } else {
            raspi.setObjectChangedDateTime(new Date());
            raspi.setObjectChangedUser(prodigaUserLoginManager.getCurrentUser());
        }

        RaspberryPi result = raspberryPiRepository.save(raspi);

        logInformationService.logForCurrentUser("Raspberry Pi " + result.getInternalId() + " saved");

        return result;
    }

    /**
     * Deletes the given Raspberry Pi
     * @param raspi The raspi to delete
     */
    @PreAuthorize("hasAuthority('ADMIN')") //NOSONAR
    public void delete(RaspberryPi raspi) throws Exception {
        if(raspi == null) {
            return;
        }

        List<Dice> assignedDices = diceService.getAllByRaspberryPi(raspi);
        if(!assignedDices.isEmpty()) {
            throw new ProdigaGeneralExpectedException(
                    "Cannot delete Raspberry Pi because there are still cubes assigned.",
                    MessageType.ERROR);
        }

        raspberryPiRepository.delete(raspi);
        logInformationService.logForCurrentUser("Raspberry Pi " + raspi.getInternalId() + " deleted!");
    }

    public RaspberryPi createRaspi(String internalId) {
        RaspberryPi raspberryPi = new RaspberryPi();
        raspberryPi.setInternalId(internalId);
        Room room = new Room();
        room.setId(null);
        raspberryPi.setAssignedRoom(room);

        return raspberryPi;
    }

    /**
     * Deletes raspberry from the pending list
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public void tryDeletePendingRaspberry(RaspberryPi raspi) {
        pendingRaspberryPis.stream()
            .filter(x -> x.getInternalId().equals(raspi.getInternalId()))
            .findFirst().ifPresent(raspiInList -> {
                pendingRaspberryPis.remove(raspiInList);
                logInformationService.logForCurrentUser("Raspberry Pi with internal ID " + raspi.getInternalId() +
                    " deleted from pending Raspberry Pis");
        });
    }
}
