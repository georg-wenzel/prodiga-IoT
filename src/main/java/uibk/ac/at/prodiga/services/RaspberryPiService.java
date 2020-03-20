package uibk.ac.at.prodiga.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import java.util.Optional;

@Component
@Scope("application")
public class RaspberryPiService {

    private final RaspberryPiRepository raspberryPiRepository;

    public RaspberryPiService(RaspberryPiRepository raspberryPiRepository) {
        this.raspberryPiRepository = raspberryPiRepository;
    }

    public Optional<RaspberryPi> findByInternalId(String internalId) {
        return raspberryPiRepository.findFirstByInternalId(internalId);
    }

    public RaspberryPi findByInternalIdAndThrow(String internalId) throws Exception {
        return raspberryPiRepository.findFirstByInternalId(internalId)
                .orElseThrow(() -> new ProdigaGeneralExpectedException(
                        "RaspberryPi with internal id " + internalId + " not " +
                                "found", MessageType.WARNING));
    }
}
