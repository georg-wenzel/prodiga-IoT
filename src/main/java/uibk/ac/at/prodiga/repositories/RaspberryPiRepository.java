package uibk.ac.at.prodiga.repositories;

import uibk.ac.at.prodiga.model.RaspberryPi;

import java.util.Optional;

public interface RaspberryPiRepository extends AbstractRepository<RaspberryPi, Long> {

    Optional<RaspberryPi> findFirstByInternalId(String internalId);

}
