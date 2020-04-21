package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
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
@Component
@Scope("application")
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RaspberryPiRepository raspberryPiRepository;
    private final LogInformationService logInformationService;

    private User getAuthenicatedUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findFirstByUsername(auth.getName());
    }

    public RoomService(RoomRepository roomRepository, UserRepository userRepository, RaspberryPiRepository raspberryPiRepository, LogInformationService logInformationService){
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.raspberryPiRepository = raspberryPiRepository;
        this.logInformationService = logInformationService;
    }

    /**
     * Returns a collection of all rooms
     * @return A collection of all rooms
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Collection<Room> getAllRooms(){
        return Lists.newArrayList(roomRepository.findAll());
    }

    /**
     * Gets the first room with the specified id. (Unique identifier)
     * @param id the id of the room
     * @return The room with this Id, or null if none exists
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Room getFirstById(long id){
        return roomRepository.findFirstById(id);
    }

    /**
     * Gets the FIRST room with the specified room name.
     * @param name the name of the room
     * @return The first room in the database which has this name, or null if none exists
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Room getFirstByName(String name){
        return roomRepository.findFirstByName(name);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Room getManagedInstance(Room room){
        return this.roomRepository.findFirstById(room.getId());
    }

    /**
     * Saves the current room in the database. If room with this ID already exists, overwrites data of existing room in the database.
     * @param room The room to save
     * @return The new state of the room after saving in the DB
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Room saveRoom(Room room) throws ProdigaGeneralExpectedException{
        if(room.getName() == null || room.getName().isEmpty()){
            throw new ProdigaGeneralExpectedException("Roomname cannot be empty", MessageType.ERROR);
        }
        if(room.getName().length() < 2 || room.getName().length() > 20) {
            throw new ProdigaGeneralExpectedException("Room name must be between 2 and 20 characters", MessageType.ERROR);
        }

        if(room.isNew()){
            if(roomRepository.findFirstByName(room.getName()) != null){
                throw new ProdigaGeneralExpectedException("Room with same name already exists.", MessageType.ERROR);
            }
            room.setObjectCreatedDateTime(new Date());
            room.setObjectCreatedUser(getAuthenicatedUser());
        }
        else{
            room.setObjectChangedDateTime(new Date());
            room.setObjectChangedUser(getAuthenicatedUser());
        }
        return roomRepository.save(room);
    }

    /**
     * Deletes the room with this ID from the database.
     * @param roomToDelete The room to delete
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteRoom(Room roomToDelete)  throws DeletionNotAllowedException
    {
        Room managedRoom = this.getManagedInstance(roomToDelete);

        if(!roomToDelete.getRaspberryPis().isEmpty()) {
            throw new DeletionNotAllowedException("Room can not be deleted if there is a Raspberry Pi in it");
        }
        roomRepository.delete(roomToDelete);
        logInformationService.log("Room " + roomToDelete.getName() + " was deleted!");
    }

    /**
     * Adds a raspberry pi to a room
     * @param room that gets the raspberry pi added
     * @param raspberryPi to be add to the given room
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addRoomToRaspberryPi(Room room, RaspberryPi raspberryPi){
        this.getManagedInstance(room).addRaspberryPi(raspberryPi);
    }

    /**
     * Removes the raspberry pi from a room
     * @param room that gets the raspberry pi removed
     * @param raspberryPi to be removed from the given room
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public void removeRoomFromRaspberryPi(Room room, RaspberryPi raspberryPi){
        this.getManagedInstance(room).removeRaspberryPi(raspberryPi);
    }

    /**
     * Loads a room by its roomname
     * @param roomname roomname to search for
     * @return a room with the given roomname
     */
    @PreAuthorize("hasAuthority('ADMIN') or principal.roomname eq #roomname")
    public Room loadRoom(String roomname) {
        return roomRepository.findFirstByName(roomname);
    }

    /**
     * Loads a room by its roomId
     * @param roomId roomId to search for
     * @return a room with the given roomId
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Room loadRoom(Long roomId){
        return roomRepository.findFirstById(roomId);
    }

    /**
     * Creates a new room
     * @return a new room
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Room createRoom(){
        Room room = new Room();
        room.setObjectCreatedDateTime(new Date());
        room.setObjectCreatedUser(new User());
        return room;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Room createNewRoom() {
        return new Room();
    }
    /**
     * Returns the amount of rooms in the db
     * @return the amount of rooms
     */
    public long getRoomCount(){
        return roomRepository.count();
    }


}
