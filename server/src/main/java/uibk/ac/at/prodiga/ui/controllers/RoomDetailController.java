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

@Component
@Scope("view")
public class RoomDetailController {
    private final RoomService roomService;

    private Room room;

    public RoomDetailController(RoomService roomService){
        this.roomService = roomService;
    }

    /**
     * Saves currently selected room
     * @throws Exception if save fails
     */
    public void saveRoom() throws Exception {
        room = roomService.saveRoom(room);
        SnackbarHelper.getInstance().showSnackBar("Room " + room.getId() + " saved!", MessageType.INFO);
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

    public void deleteRoom(Room room) throws DeletionNotAllowedException {
        roomService.deleteRoom(room);
    }

    public void addRoomToRaspberryPi(Room room, RaspberryPi raspberryPi){
        roomService.addRoomToRaspberryPi(room,raspberryPi);
    }

    public void removeRoomFromRaspberryPi(Room room, RaspberryPi raspberryPi){
        roomService.removeRoomFromRaspberryPi(room,raspberryPi);
    }

    public Room createRoom(){
        return roomService.createRoom();
    }

    public long getRoomCount(){
        return roomService.getRoomCount();
    }

    public Room getRoom() {
        return this.room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getRoomByName() {
        if(this.room == null) {
            return null;
        }
        return room.getName();
    }

    public void setRoomByName(String name) throws Exception {
        doReloadRoom(name);
    }
}
