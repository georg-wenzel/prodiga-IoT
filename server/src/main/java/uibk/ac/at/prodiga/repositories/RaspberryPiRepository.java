package uibk.ac.at.prodiga.repositories;

import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.Room;

import java.util.Collection;
import java.util.Optional;

public interface RaspberryPiRepository extends AbstractRepository<RaspberryPi, Long> {

    Optional<RaspberryPi> findFirstByInternalId(String internalId);
    Collection<RaspberryPi> findAllByAssignedRoom(Room room);

}
