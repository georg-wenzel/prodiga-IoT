package uibk.ac.at.prodigaclient.tests;

import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import tinyb.BluetoothDevice;
import uibk.ac.at.prodigaclient.BluetoothUtility.Cube;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CubeTest {
    private Cube cube;
    private BluetoothDevice bluetoothDevice;
    private List<byte[]> historyList;

    @BeforeEach
    public void setUp() {
        historyList = new LinkedList<>();
        historyList.add(new byte[]{0x0d, 0x00, 0x18, 0x3c, 0x00, 0x04, 0x10, 0x00, 0x04, 0x10, 0x00, 0x08,
                0x16, 0x00, 0x0c, 0x32, 0x00, 0x10, 0x0d, 0x00, 0x14});
        historyList.add(new byte[]{0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        historyList.add(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

        bluetoothDevice = BluetoothDeviceMockCreator.mockFullBluetoothDevice("12:34:56:78:90:12", "TimeFlip", historyList, new byte[] {0x01}, new byte[]{0x17});
        cube = new Cube(bluetoothDevice);
    }

    public void verifyPasswordEntered() {
        verify(bluetoothDevice.find("f1196f50-71a4-11e6-bdf4-0800200c9a66", Duration.ofSeconds(1))
                              .find("f1196f57-71a4-11e6-bdf4-0800200c9a66", Duration.ofSeconds(1)),
                              times(1)
              ).writeValue(new byte[]{0x30, 0x30, 0x30, 0x30, 0x30, 0x30});
    }


    public void verifyCommand(byte[] command) {
        verify(bluetoothDevice.find("f1196f50-71a4-11e6-bdf4-0800200c9a66", Duration.ofSeconds(1)).
                               find("f1196f54-71a4-11e6-bdf4-0800200c9a66", Duration.ofSeconds(1)),
                               times(1)
              ).writeValue(command);
    }

    @Test
    public void getCubeNameTest() {
        Assertions.assertEquals("TimeFlip", cube.getName());
    }

    @Test
    public void getCubeAddressTest() {
        Assertions.assertEquals("12:34:56:78:90:12", cube.getAddress());
    }

    @Test
    public void getCurrentSideTest() {
        Assertions.assertEquals(1, cube.getCurrentSide());
        verifyPasswordEntered();
    }

    @Test
    public void getHistorySizeTest() {
        List<HistoryEntry> historyEntryList = cube.getHistory();
        Assertions.assertEquals(7, historyEntryList.size());
        verifyPasswordEntered();
        verifyCommand(new byte[]{0x01});
    }

    @Test
    public void getHistoryRightEntriesTest() {
        List<HistoryEntry> historyEntries = cube.getHistory();
        List<HistoryEntry> expectedHistoryEntries = new LinkedList<>();

        for (int i = 0; i < historyList.size() - 2; i++) { // size - 2 because the last two lines are
            byte [] list = historyList.get(i);
            for (int j = 0; j < 7; j++) { // decode the list by hand.
                expectedHistoryEntries.add(new HistoryEntry(Arrays.copyOfRange(list, 3 * j, 3 * (j + 1))));
            }
        }

        Assertions.assertEquals(expectedHistoryEntries.size(), historyEntries.size());

        for (int i = 0; i < historyEntries.size(); i++) {
            Assertions.assertEquals(expectedHistoryEntries.get(i).getID(), historyEntries.get(i).getID());
            Assertions.assertEquals(expectedHistoryEntries.get(i).getSeconds(), historyEntries.get(i).getSeconds());
        }

        verifyPasswordEntered();
        verifyCommand(new byte[]{0x01});
    }

    @Test
    public void deleteCubeHistoryTest() {
        cube.deleteHistory();

        verifyPasswordEntered();
        verifyCommand(new byte[]{0x02});
    }

    @Test
    public void connectToCubeTest() {
        bluetoothDevice = BluetoothDeviceMockCreator.mockConnectionTestBluetoothDevice(false);
        cube = new Cube(bluetoothDevice);

        cube.failsafeConnect();
        verify(bluetoothDevice, times(1)).getConnected();
        verify(bluetoothDevice, times(1)).connect();
    }

    @Test
    public void diconnectFromCubeTest() {
        bluetoothDevice = BluetoothDeviceMockCreator.mockConnectionTestBluetoothDevice(true);
        cube = new Cube(bluetoothDevice);

        cube.failsafeDisconnect();
        verify(bluetoothDevice, times(1)).getConnected();
        verify(bluetoothDevice, times(1)).disconnect();
    }

    @Test
    public void brokenConnectToCubeTest() {
        bluetoothDevice = BluetoothDeviceMockCreator.mockBrokenConnectionTestBluetoothDevice(false);
        cube = new Cube(bluetoothDevice);

        cube.failsafeConnect();
        verify(bluetoothDevice, times(1)).getConnected();
        verify(bluetoothDevice, times(3)).connect();
    }

    @Test
    public void brokenDisconnectFromCubeTest() {
        bluetoothDevice = BluetoothDeviceMockCreator.mockBrokenConnectionTestBluetoothDevice(true);
        cube = new Cube(bluetoothDevice);

        cube.failsafeDisconnect();
        verify(bluetoothDevice, times(1)).getConnected();
        verify(bluetoothDevice, times(3)).disconnect();
    }
}
