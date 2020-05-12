package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import org.mockito.*;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CubeManagerTest {

    private CubeManager cubeManagerMock = mock(CubeManager.class);

    @BeforeEach
    public void setUp() {
        Set<String> set = new HashSet<>();
        set.add("0C:61:CF:C7:8F:D5");
        set.add("0C:61:CF:C7:8F:D6");

        when(cubeManagerMock.getCubeIDList()).thenReturn(set); // addet a set of cubes

        when(cubeManagerMock.getCurrentSide("0C:61:CF:C7:8F:D5")).thenReturn(1); // cube 1 current side = 1
        when(cubeManagerMock.getCurrentSide("0C:61:CF:C7:8F:D6")).thenReturn(12); // cube 2 current side = 12

        when(cubeManagerMock.getBattery("0C:61:CF:C7:8F:D5")).thenReturn(45); // cube 1 battery = 45
        when(cubeManagerMock.getBattery("0C:61:CF:C7:8F:D6")).thenReturn(90); // cube 2 battery = 90

        List<HistoryEntry> historyEntryListOne = new LinkedList<>();
        List<HistoryEntry> historyEntryListTwo = new LinkedList<>();

        historyEntryListOne.add(new HistoryEntry(new byte[]{0x0d, 0x00, 0x18})); // Side 6 -> Seconds 13
        historyEntryListOne.add(new HistoryEntry(new byte[]{0x3c, 0x00, 0x04})); // Side 1 -> Seconds 60

        historyEntryListTwo.add(new HistoryEntry(new byte[]{0x10, 0x00, 0x04})); // Side 1 -> Seconds 16
        historyEntryListTwo.add(new HistoryEntry(new byte[]{0x10, 0x00, 0x08})); // Side 2 -> Seconds 16

        when(cubeManagerMock.getHistory("0C:61:CF:C7:8F:D5")).thenReturn(historyEntryListOne); // cube 1 history list one
        when(cubeManagerMock.getHistory("0C:61:CF:C7:8F:D6")).thenReturn(historyEntryListTwo); // cube 2 history list two
    }

    @Test
    void testCubeManagerMock() {
        Assertions.assertEquals(1, cubeManagerMock.getCurrentSide("0C:61:CF:C7:8F:D5"));
    }
}
