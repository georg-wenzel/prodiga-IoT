package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uibk.ac.at.prodigaclient.BluetoothUtility.Cube;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;

import java.util.LinkedList;
import java.util.List;

public class CubeTest {
    private Cube cube;

    // 4 Stunden
    @BeforeEach
    public void setUp() {
        List<byte[]> historyList = new LinkedList<>();
        historyList.add(new byte[]{0x0d, 0x00, 0x18, 0x3c, 0x00, 0x04, 0x10, 0x00, 0x04, 0x10, 0x00, 0x08,
                0x16, 0x00, 0x0c, 0x32, 0x00, 0x10, 0x0d, 0x00, 0x14});
        historyList.add(new byte[]{0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        historyList.add(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

        BluetoothDeviceMockCreator bdmc = new BluetoothDeviceMockCreator("12:34:56:78:90:12", "TimeFlip", historyList, new byte[] {0x01});

        cube = new Cube(bdmc.mockBluetoothDevice());
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
    }

    @Test
    public void getHistoryTest() {
        List<HistoryEntry> historyEntryList = cube.getHistory();
        System.out.println(historyEntryList);
        Assertions.assertEquals(7, historyEntryList.size());
    }
}
