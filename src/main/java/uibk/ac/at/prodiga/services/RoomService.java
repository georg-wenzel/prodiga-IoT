package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uibk.ac.at.prodiga.model.Room;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.RoomRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Date;

/*
Anlegen, abfragen, bearbeiten und löschen von Räumen.
Checken was passiert, wenn noch ein Raspi/Würfel in dem Raum is usw...
 */
@Service
@Scope("application")
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private User getAuthenicatedUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findFirstByUsername(auth.getName());
    }

    public RoomService(RoomRepository roomRepository, UserRepository userRepository){
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Collection<Room> getAllRooms(){
        return Lists.newArrayList(roomRepository.findAll());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Room saveRoom(Room room) throws ProdigaGeneralExpectedException{
        if(room.getName() == null || room.getName().isEmpty()){
            throw new ProdigaGeneralExpectedException("No room name found", MessageType.ERROR);
        }
        if(room.getName().length() < 2 || room.getName().length() > 20) {
            throw new ProdigaGeneralExpectedException("Room name must be between 2 and 20 characters", MessageType.ERROR);
        }

        if(room.isNew()){
            room.setObjectCreatedDateTime(new Date());
            room.setObjectCreatedUser(getAuthenicatedUser());
        }
        return roomRepository.save(room);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public void deleteRoom(Room roomToDelete) /* throws DeletionNotAllowedException */
    {
      /*  if(!roomToDelete.getRaspi().isEmpty) {
           throw new DeletionNotAllowedException("Room can not be deleted if there is a Raspberry Pi in it");
          }
       */
      roomRepository.delete(roomToDelete);
    }


}
