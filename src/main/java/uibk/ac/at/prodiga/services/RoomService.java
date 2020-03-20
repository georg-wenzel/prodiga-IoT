package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Room;
import uibk.ac.at.prodiga.repositories.RoomRepository;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import java.util.Collection;

/*
Anlegen, abfragen, bearbeiten und löschen von Räumen.
Checken was passiert, wenn noch ein Raspi/Würfel in dem Raum is usw...
 */
@Component
@Scope("application")
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository){
        this.roomRepository = roomRepository;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Collection<Room> getAllRooms(){
        return Lists.newArrayList(roomRepository.findAll());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void saveRoom(Room room) throws ProdigaGeneralExpectedException{
        if(room.getName().length() < 2) {
            throw new ProdigaGeneralExpectedException("Room name must be longer than 1 character", MessageType.ERROR);
        }
    }

}
