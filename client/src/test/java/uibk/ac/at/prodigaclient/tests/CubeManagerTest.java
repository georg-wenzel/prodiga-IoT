package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;
import uibk.ac.at.prodigaclient.tests.MockCreators.BluetoothManagerMockCreator;
import uibk.ac.at.prodigaclient.tests.MockCreators.CubeManagerCreator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CubeManagerTest {
    BluetoothManager mockManager;

    CubeManager cubeManager;

    @BeforeEach
    public void setUp() {
        mockManager = BluetoothManagerMockCreator.mockBluetoothManager();
        cubeManager = CubeManagerCreator.createCustomCubeManagerInstance(mockManager);
    }

    public void verifyCommand(BluetoothDevice bluetoothDevice, byte[] command) {
        verify(bluetoothDevice.find("f1196f50-71a4-11e6-bdf4-0800200c9a66", Duration.ofSeconds(1))
                              .find("f1196f54-71a4-11e6-bdf4-0800200c9a66", Duration.ofSeconds(1)),
                               times(1)
              ).writeValue(command);
    }

    @Test
    public void constructorTest() {
        Assertions.assertNotNull(cubeManager);
    }


    @Test
    public void getCubeIDListTest() {
        cubeManager.updateDeviceList();
        Set<String> availableCubes = cubeManager.getCubeIDList();

        Assertions.assertNotNull(availableCubes);
        Assertions.assertEquals(2, availableCubes.size());
        Assertions.assertTrue(availableCubes.contains("12:34:56:78:90:00"));
        Assertions.assertTrue(availableCubes.contains("12:34:56:78:90:01"));
    }


    @Test
    public void getHistoryTest() {
        cubeManager.updateDeviceList();

        List<HistoryEntry> historyEntryListOne = cubeManager.getHistory("12:34:56:78:90:00");
        List<HistoryEntry> historyEntryListTwo = cubeManager.getHistory("12:34:56:78:90:01");

        Assertions.assertNotNull(historyEntryListOne);
        Assertions.assertEquals(7, historyEntryListOne.size());

        Assertions.assertNotNull(historyEntryListTwo);
        Assertions.assertEquals(7, historyEntryListTwo.size());

        for (BluetoothDevice bluetoothDevice : mockManager.getDevices().subList(0, 2)) {
            verify(bluetoothDevice, times(1)).connect();
        }
    }

    @Test
    public void getBatteryTest() {
        cubeManager.updateDeviceList();

        int batteryOne = cubeManager.getBattery("12:34:56:78:90:00");
        int batteryTwo = cubeManager.getBattery("12:34:56:78:90:01");

        Assertions.assertEquals(20, batteryOne);

        Assertions.assertEquals(37, batteryTwo);

        for (BluetoothDevice bluetoothDevice : mockManager.getDevices().subList(0, 2)) {
            verify(bluetoothDevice, times(1)).connect();
        }
    }


    @Test
    public void getDeleteHistory() {
        cubeManager.updateDeviceList();

        cubeManager.deleteHistory("12:34:56:78:90:00");
        cubeManager.deleteHistory("12:34:56:78:90:01");

        for (BluetoothDevice bluetoothDevice : mockManager.getDevices().subList(0, 2)) {
            verify(bluetoothDevice, times(1)).connect();
            verifyCommand(bluetoothDevice, new byte[]{0x02});
        }
    }

    @Test
    public void getCurrentSideTest() {
        cubeManager.updateDeviceList();

        int batteryOne = cubeManager.getCurrentSide("12:34:56:78:90:00");
        int batteryTwo = cubeManager.getCurrentSide("12:34:56:78:90:01");

        Assertions.assertEquals(1, batteryOne);

        Assertions.assertEquals(2, batteryTwo);

        for (BluetoothDevice bluetoothDevice : mockManager.getDevices().subList(0, 2)) {
            verify(bluetoothDevice, times(0)).connect();
            verify(bluetoothDevice, times(0)).disconnect();
        }
    }


    @Test
    public void connectToDeviceTest() {
        mockManager = BluetoothManagerMockCreator.mockBluetoothManagerConnectionTest(false);
        cubeManager = CubeManagerCreator.createCustomCubeManagerInstance(mockManager);

        cubeManager.updateDeviceList();

        for (String s : cubeManager.getCubeIDList()) {
            cubeManager.connectToCube(s);
        }

        verify(mockManager.getDevices().get(0), times(1)).connect();
    }


    @Test
    public void disconnectFromDeviceTest() {
        mockManager = BluetoothManagerMockCreator.mockBluetoothManagerConnectionTest(true);
        cubeManager = CubeManagerCreator.createCustomCubeManagerInstance(mockManager);

        cubeManager.updateDeviceList();

        for (String s : cubeManager.getCubeIDList()) {
            cubeManager.disconnectFromCube(s);
        }

        verify(mockManager.getDevices().get(0), times(1)).disconnect();
    }
}
