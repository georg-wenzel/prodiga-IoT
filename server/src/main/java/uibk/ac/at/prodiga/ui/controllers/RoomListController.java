package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import uibk.ac.at.prodiga.model.Room;
import uibk.ac.at.prodiga.services.RoomService;

import java.io.Serializable;
import java.util.Collection;

@Component
@Scope("view")
public class RoomListController implements Serializable {

    private static final long serialVersionUID = 5325687682292577315L;

    private final RoomService roomService;
    private Collection<Room> rooms;

    public RoomListController(RoomService roomService){this.roomService = roomService;}

    public Collection<Room> getRooms(){
        if(rooms == null) rooms = roomService.getAllRooms();
        return rooms;
    }

    public void forceRefresh()
    {
        this.rooms = null;
    }
}
