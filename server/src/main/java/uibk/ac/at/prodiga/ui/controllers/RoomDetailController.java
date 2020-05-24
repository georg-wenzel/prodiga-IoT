package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import uibk.ac.at.prodiga.exceptions.DeletionNotAllowedException;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.Room;
import uibk.ac.at.prodiga.services.RoomService;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.SnackbarHelper;

import java.io.Serializable;

@Component
@Scope("view")
public class RoomDetailController implements Serializable {

    private static final long serialVersionUID = 5325687699692577315L;

    private final RoomService roomService;

    private Room room;

    public RoomDetailController(RoomService roomService){
        this.roomService = roomService;
    }

    /**
     * Action to delete the currently displayed user.
     */
    public void doDeleteRoom() throws Exception {
        this.roomService.deleteRoom(room);
        SnackbarHelper.getInstance()
                .showSnackBar("Room " + room.getName() + " deleted!", MessageType.ERROR);
    }

    /**
     * Saves a room in the database. If an object with this ID already exists, overwrithes the object's data at this ID
     * ProdigaGeneralExpectedException Is thrown when name is not between 2 and 20 characters or does not exist
     */
    public void doSaveRoom()throws Exception{
        room = this.roomService.saveRoom(room);
        SnackbarHelper.getInstance().showSnackBar("Room " + room.getName() + " saved!", MessageType.INFO);
    }

    public void doReloadRoom(String roomname) throws Exception {
        if (roomname != null && !roomname.trim().isEmpty()) {
            this.room = roomService.loadRoom(roomname);
        } else {
            this.room = roomService.createRoom();
        }
    }



    public Room getManagedInstance(){
        return roomService.getManagedInstance(room);
    }

    /**
     * Returns the first room with a matching name (unique identifier)
     * @param name The name of the room
     * @return The first(and only) room with a matching name, or null if none was found
     */
    public Room getFirstByName(String name){
        return roomService.getFirstByName(name);
    }

    /**
     * Returns the first room with a matching id (unique identifier)
     * @param id The id of the room
     * @return The first(and only) room with a matching id, or null if none was found
     */
    public Room getFirstById(long id){
        return roomService.getFirstById(id);
    }

    public long getRoomCount(){
        return roomService.getRoomCount();
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) throws Exception {
        this.room = room;
        doReloadRoom(room.getName());
    }

    public String getRoomByName() {
        if(this.room == null) {
            return null;
        }
        return room.getName();
    }

    /**
     * Gets room by id.
     * @return the room by id
     */
    public Long getRoomById(){
        if(this.room == null){
            return (long) -1;
        }
        return this.room.getId();
    }

    /**
     * Sets current room by roomId
     * @param roomId teamId to be set
     */
    public void setRoomById(Long roomId){
        loadRoomById(roomId);
    }


    /**
     * Sets currently active room by the id
     * @param roomId when roomId could not be found
     */
    public void loadRoomById(Long roomId){
        if(roomId == null){
            this.room = roomService.createNewRoom();
        } else {
            this.room = roomService.loadRoom(roomId);
        }
    }


    /**
     * Sets current room by roomId
     * @param roomname teamId to be set
     */
    public void setRoomByName(String roomname){
        loadRoomByName(roomname);
    }

    public void loadRoomByName(String roomname){
        if(roomname == null){
            this.room = roomService.createRoom();
        } else {
            this.room = roomService.loadRoom(roomname);
        }
    }



}
