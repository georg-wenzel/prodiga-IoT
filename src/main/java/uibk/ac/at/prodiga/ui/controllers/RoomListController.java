package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uibk.ac.at.prodiga.model.Room;
import uibk.ac.at.prodiga.services.RoomService;

import java.io.Serializable;
import java.util.Collection;

@Controller
@Scope("view")
public class RoomListController implements Serializable {

    private final RoomService roomService;

    public RoomListController(RoomService roomService){this.roomService = roomService;}

    public Collection<Room> getRooms(){return roomService.getAllRooms();}
}
