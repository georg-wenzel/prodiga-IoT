package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;
import uibk.ac.at.prodigaclient.tests.MockCreators.BluetoothManagerMockCreator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CubeManagerTest {
    BluetoothManager mockManager;

    CubeManager cubeManager;

    @BeforeEach
    public void setUp() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<CubeManager> c = CubeManager.class.getDeclaredConstructor(BluetoothManager.class); // whoa didn't know this works
        c.setAccessible(true);

        mockManager = BluetoothManagerMockCreator.mockBluetoothManager();

        cubeManager = c.newInstance(mockManager);
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

        List<BluetoothDevice> bluetoothDevices = mockManager.getDevices().subList(0, 2); // device 2 isn't a TimeFlipCube

        for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
            verify(bluetoothDevice, times(1)).connect();
        }
    }

    @Test
    public void getBatteryTest() {
        cubeManager.updateDeviceList();

        int batteryOne = cubeManager.getBattery("12:34:56:78:90:00");
        int batteryTwo = cubeManager.getBattery("12:34:56:78:90:01");

//        Assertions.assertEquals(1, );
//
//        Assertions.assertEquals(7, historyEntryListTwo.size());

        List<BluetoothDevice> bluetoothDevices = mockManager.getDevices().subList(0, 2); // device 2 isn't a TimeFlipCube

        for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
            verify(bluetoothDevice, times(1)).connect();
        }
    }
}
