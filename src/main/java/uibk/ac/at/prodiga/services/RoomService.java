package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uibk.ac.at.prodiga.exceptions.DeletionNotAllowedException;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.Room;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
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
    private final RaspberryPiRepository raspberryPiRepository;

    private User getAuthenicatedUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findFirstByUsername(auth.getName());
    }

    public RoomService(RoomRepository roomRepository, UserRepository userRepository, RaspberryPiRepository raspberryPiRepository){
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.raspberryPiRepository = raspberryPiRepository;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Collection<Room> getAllRooms(){
        return Lists.newArrayList(roomRepository.findAll());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Room getFirstById(long id){
        return roomRepository.findFirstById(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Room getFirstByName(String name){
        return roomRepository.findFirstByName(name);
    }

    @Transactional
    public Room getManagedInstance(Room room){
        return this.roomRepository.findFirstById(room.getId());
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
    public void deleteRoom(Room roomToDelete)  throws DeletionNotAllowedException
    {
        Room managedRoom = this.getManagedInstance(roomToDelete);

        if(!roomToDelete.getRaspberryPis().isEmpty()) {
            throw new DeletionNotAllowedException("Room can not be deleted if there is a Raspberry Pi in it");
        }
        roomRepository.delete(roomToDelete);
    }

    @Transactional
    public void addRoomToRaspberryPi(Room room, RaspberryPi raspberryPi){
        this.getManagedInstance(room).addRaspberryPi(raspberryPi);
    }

    @Transactional
    public void removeRoomFromRaspberryPi(Room room, RaspberryPi raspberryPi){
        this.getManagedInstance(room).removeRaspberryPi(raspberryPi);
    }

    @Transactional
    public Room createRoom(){
        Room room = new Room();
        room.setObjectCreatedDateTime(new Date());
        room.setObjectCreatedUser(new User());
        return room;
    }

    public long getRoomCount(){
        return roomRepository.count();
    }


}
