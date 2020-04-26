package uibk.ac.at.prodigaclient.BluetoothUtility;

import tinyb.*;

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

    private CubeManager() {
        manager = BluetoothManager.getBluetoothManager();
        listOfCubes = new HashMap<>();
        discoveryStarted = manager.startDiscovery();
    }

    private static class SingletonHolder {
        private final static CubeManager INSTANCE = new CubeManager();
    }

    public static CubeManager getInstance() {
        return SingletonHolder.INSTANCE;
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
                    .map(Cube::new).collect(Collectors.toMap(Cube::getAddress, Function.identity()));
        }
    }

    public List<HistoryEntry> getHistory(String cubeID) {
        Cube cube = listOfCubes.get(cubeID);
        List<HistoryEntry> historyEntryList;

        cube.failsafeConnect();

        historyEntryList = cube.getHistory();

        cube.failsafeDisconnect();

        return historyEntryList;
    }

    public int getBattery(String cubeID) {
        Cube cube = listOfCubes.get(cubeID);
        int batteryPercent;

        cube.failsafeConnect();

        batteryPercent = cube.getBattery();

        cube.failsafeDisconnect();

        return batteryPercent;
    }

    public int getCurrentSide(String cubeID) {
        Cube cube = listOfCubes.get(cubeID);
        int currentSide;

        currentSide = cube.getCurrentSide();

        return currentSide;
    }

    public void connectToCube(String cubeID) {
        Cube cube = listOfCubes.get(cubeID);
        cube.failsafeConnect();
    }

    public void disconnectFromCube(String cubeID) {
        Cube cube = listOfCubes.get(cubeID);
        cube.failsafeDisconnect();
    }
}
