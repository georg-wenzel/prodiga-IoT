package uibk.ac.at.prodigaclient;

import tinyb.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ManageCubes {
    private BluetoothManager manager;
    private Map<String, Cube> listOfCubes;
    private boolean discoveryStarted;

    public ManageCubes () {
        manager = BluetoothManager.getBluetoothManager();
        listOfCubes = new HashMap<>();
        discoveryStarted = manager.startDiscovery();
    }

    public void updateDeviceList() {
        List<BluetoothDevice> list = manager.getDevices();

        if (list != null) {
            // get a list of all current Cubes in the area
            // TODO: test if all devices are shown instantly or we have to add a delay
            // first filter than map to Cube because of memory saves
            listOfCubes = list.stream().filter(x -> x.getName().toLowerCase().contains("timeflip"))
                    .map(Cube::new).collect(Collectors.toMap(Cube::getName, Function.identity()));
        }
    }
}
