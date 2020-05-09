package uibk.ac.at.prodigaclient.BluetoothUtility;

import java.util.List;
import java.util.Set;

public interface Manager {
    Set<String> getCubeIDList();

    void updateDeviceList();

    List<HistoryEntry> getHistory(String cubeID);

    int getBattery(String cubeID);

    int getCurrentSide(String cubeID);

    void deleteHistory(String cubeID);

    void connectToCube(String cubeID);

    void disconnectFromCube(String cubeID);
}
