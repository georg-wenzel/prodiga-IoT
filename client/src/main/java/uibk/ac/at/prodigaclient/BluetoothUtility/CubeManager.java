package uibk.ac.at.prodigaclient.BluetoothUtility;

import tinyb.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cube Managing class
 * This is an easy interface to the cube class and keeps track of all the cubes in the near surrounding.
 */
public class CubeManager implements Manager {
    /**
     * The interface to the bluetooth module on raspberry pi
     */
    private BluetoothManager manager;

    /**
     * List of cubes in the near surrounding
     */
    private Map<String, Cube> listOfCubes;

    /**
     * Saves the state of the discovery modus
     */
    private boolean discoveryStarted;

    /**
     * Constructor of the Cub manager class.
     * Creates the list of cubes and the bluetooth manager
     * and starts the discovery modus
     */
    private CubeManager() {
        manager = BluetoothManager.getBluetoothManager();
        listOfCubes = new HashMap<>();
        discoveryStarted = manager.startDiscovery();
    }

    /**
     * Cube manager should only exists once
     * This is the private Singelton class which takes care of it
     */
    private static class SingletonHolder {
        private final static CubeManager INSTANCE = new CubeManager();
    }

    /**
     * Get the singel instance of the cube
     * @return Instance of the Manager cube
     */
    public static CubeManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * When closing the manager this should be called to stop the discovery mode
     */
    public void closeManager() {
        manager.stopDiscovery();
    }

    /**
     * Gets a list of all near cube id's
     * @return list of cube id's
     */
    public Set<String> getCubeIDList() {
        return listOfCubes.keySet();
    }

    /**
     * updates the internal list of cubes
     * A cube is specified as a cube if and only if the cube contains timeflip (case insensitive) in it's name.
     */
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

    /**
     * An interface function to the cubes getHistory function.
     * Get the history of a cube provided his cube ID
     * @param cubeID Cubes identifier from which the history should be read
     * @return List of HistoryEntry's from the specified cube ID
     */
    public List<HistoryEntry> getHistory(String cubeID) {
        Cube cube = listOfCubes.get(cubeID);
        List<HistoryEntry> historyEntryList;

        cube.failsafeConnect();

        historyEntryList = cube.getHistory();

        cube.failsafeDisconnect();

        return historyEntryList;
    }

    /**
     * An interface function to the cubes getBattery function.
     * get the battery percent of the given cube
     * @param cubeID Cubes identifier from which the battery should be read
     * @return battery in percent
     */
    public int getBattery(String cubeID) {
        Cube cube = listOfCubes.get(cubeID);
        int batteryPercent;

        cube.failsafeConnect();

        batteryPercent = cube.getBattery();

        cube.failsafeDisconnect();

        return batteryPercent;
    }

    /**
     * An interface function to the cubes deleteHistory function.
     * deletes the history of a given cube.
     * @param cubeID Cubes identifier from which the current side should be read
     */
    public void deleteHistory(String cubeID) {
        Cube cube = listOfCubes.get(cubeID);

        cube.failsafeConnect();

        cube.deleteHistory();

        cube.failsafeDisconnect();
    }

    /**
     * An interface function to the cubes getBattery function.
     * get the current side of the given cube
     * @param cubeID Cubes identifier from which the current side should be read
     * @return current side ID
     */
    public int getCurrentSide(String cubeID) {
        Cube cube = listOfCubes.get(cubeID);
        int currentSide;

        currentSide = cube.getCurrentSide();

        return currentSide;
    }

    /**
     * Manually connect to a cube
     * Important for the get current Side
     * @param cubeID Connect to the provided cube ID
     */
    public void connectToCube(String cubeID) {
        Cube cube = listOfCubes.get(cubeID);
        cube.failsafeConnect();
    }

    /**
     * Manually disconnect to a cube
     * Important for the get current Side
     * @param cubeID Connect to the provided cube ID
     */
    public void disconnectFromCube(String cubeID) {
        Cube cube = listOfCubes.get(cubeID);
        cube.failsafeDisconnect();
    }
}
