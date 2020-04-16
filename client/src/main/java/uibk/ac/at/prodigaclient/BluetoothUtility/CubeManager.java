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
            // first filter than map to Cube because of memory saves
            // second filter only for edge case. I will not use it unless someone specifically asked for it.
            listOfCubes = list.stream().filter(x -> x.getName().toLowerCase().contains("timeflip"))
                    .map(Cube::new)/*.filter(Cube::isCube)*/.collect(Collectors.toMap(Cube::getAddress, Function.identity()));
        }
    }

    public List<HistoryEntry> getHistory(String cubeID) {
        List<HistoryEntry> historyEntryList;
        Cube cube = listOfCubes.get(cubeID);

        cube.failsafeConnect();

        historyEntryList = cube.getHistory();

        cube.failsafeDisconnect();

        return historyEntryList;
    }

    public int getBattery(String cubeID) {
        int batteryPercent;
        Cube cube = listOfCubes.get(cubeID);

        cube.failsafeConnect();

        batteryPercent = cube.getBattery();

        cube.failsafeDisconnect();
        return batteryPercent;
    }
}
