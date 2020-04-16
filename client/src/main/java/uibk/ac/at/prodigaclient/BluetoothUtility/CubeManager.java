package uibk.ac.at.prodigaclient.BluetoothUtility;

import tinyb.*;
import uibk.ac.at.prodigaclient.BluetoothUtility.Cube;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CubeManager {
    private BluetoothManager manager;
    private Map<String, Cube> listOfCubes;
    private boolean discoveryStarted;

    public CubeManager() {
        manager = BluetoothManager.getBluetoothManager();
        listOfCubes = new HashMap<>();
        discoveryStarted = manager.startDiscovery();
    }

    public void closeManager() {
        manager.stopDiscovery();
    }

    public Set<String> getCubeIDList() {
        return listOfCubes.keySet();
    }

    public void updateDeviceList() {
        List<BluetoothDevice> list = manager.getDevices();

        if (list != null) {
            // get a list of all current Cubes in the area
            // TODO: test if all devices are shown instantly or we have to add a delay
            // first filter than map to Cube because of memory saves
            listOfCubes = list.stream().filter(x -> x.getName().toLowerCase().contains("timeflip"))
                    .map(Cube::new).collect(Collectors.toMap(Cube::getAddress, Function.identity()));
        }
    }

    public List<HistoryEntry> getHistory(String cubeID) {
        return listOfCubes.get(cubeID).getHistory();
    }

    public int getBattery(String cubeID) {
        return listOfCubes.get(cubeID).getBattery();
    }
}
