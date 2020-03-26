package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.RaspberryPi;
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

    private final List<RaspberryPi> pendingRapsberryPis = Collections.synchronizedList(new ArrayList<>());

    public RaspberryPiService(RaspberryPiRepository raspberryPiRepository, ProdigaUserLoginManager prodigaUserLoginManager) {
        this.raspberryPiRepository = raspberryPiRepository;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
    }

    /**
     * Finds the raspberry pi by the given internal id
     * @param internalId The internal id
     * @return An Optional with the found raspberry
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
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
                                "found", MessageType.WARNING));
    }

    /**
     * Returns all raspberry pis which are not configured
     * @return A list of raspberry pis
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public List<RaspberryPi> getAllPendingRaspberryPis() {
        // Make a copy here, so the reference is not thread safe any more
        return new ArrayList<>(pendingRapsberryPis);
    }

    /**
     * Retturns a list of all raspberry pis which are configured
     * @return A list of raspberry pis
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public List<RaspberryPi> getAllConfiguredRaspberryPis() {
        return Lists.newArrayList(raspberryPiRepository.findAll());
    }

    /**
     * Adds a new raspberry to the pending list
     * @param internalId The raspberry pis internal ID
     */
    public void addPendingRaspberry(String internalId) {
        RaspberryPi raspi = new RaspberryPi();
        raspi.setInternalId(internalId);
        pendingRapsberryPis.add(raspi);
    }

    /**
     * Saves the given raspberry pi
     * @param raspi The raspberry pi to save
     * @return The saved raspberry pi
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public RaspberryPi save(RaspberryPi raspi) throws Exception {

        // First check if there is a room
        if(raspi.getAssignedRoom() == null) {
            throw new ProdigaGeneralExpectedException("Cannot save Raspberry Pi without room!", MessageType.WARNING);
        }

        // Next check if the password is set
        if(StringUtils.isEmpty(raspi.getPassword())) {
            throw new ProdigaGeneralExpectedException("Cannot save Raspberry Pi with empty password!", MessageType.WARNING);
        }

        // Check if internal ID is set
        if(StringUtils.isEmpty(raspi.getInternalId())) {
            throw new ProdigaGeneralExpectedException("Cannot save Raspberry Pi with empty Internal ID", MessageType.WARNING);
        }

        if(raspi.isNew()) {
            // If the raspi is new we have to hash the password here
            raspi.setPassword(Constants.PASSWORD_ENCODER.encode(raspi.getPassword()));

            raspi.setObjectCreatedDateTime(new Date());
            raspi.setObjectCreatedUser(prodigaUserLoginManager.getCurrentUser());
        }

        raspi.setObjectChangedDateTime(new Date());
        raspi.setObjectChangedUser(prodigaUserLoginManager.getCurrentUser());

        return raspberryPiRepository.save(raspi);
    }
}
