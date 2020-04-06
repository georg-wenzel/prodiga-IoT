package uibk.ac.at.prodiga.repositories;

import uibk.ac.at.prodiga.model.Room;

public interface RoomRepository extends AbstractRepository<Room, Long> {
    //Magic methods
    Room findFirstById(Long id);
    Room findFirstByName(String name);
}
