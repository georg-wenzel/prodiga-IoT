package uibk.ac.at.prodigaclient.tests.MockCreators;

import org.mockito.MockitoAnnotations;
import tinyb.BluetoothDevice;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CubeManagerMockCreator {
    public static CubeManager mockFullCubeManager() {
        CubeManager cubeManager = mock(CubeManager.class);
        Set<String> set = new HashSet<>();
        set.add("0C:61:CF:C7:8F:D5");
        set.add("0C:61:CF:C7:8F:D6");

        when(cubeManager.getCubeIDList()).thenReturn(set); // addet a set of cubes

        when(cubeManager.getCurrentSide("0C:61:CF:C7:8F:D5")).thenReturn(1); // cube 1 current side = 1
        when(cubeManager.getCurrentSide("0C:61:CF:C7:8F:D6")).thenReturn(12); // cube 2 current side = 12

        when(cubeManager.getBattery("0C:61:CF:C7:8F:D5")).thenReturn(45); // cube 1 battery = 45
        when(cubeManager.getBattery("0C:61:CF:C7:8F:D6")).thenReturn(90); // cube 2 battery = 90

        List<HistoryEntry> historyEntryListOne = new LinkedList<>();
        List<HistoryEntry> historyEntryListTwo = new LinkedList<>();

        historyEntryListOne.add(new HistoryEntry(new byte[]{0x0d, 0x00, 0x18})); // Side 6 -> Seconds 13
        historyEntryListOne.add(new HistoryEntry(new byte[]{0x3c, 0x00, 0x04})); // Side 1 -> Seconds 60

        historyEntryListTwo.add(new HistoryEntry(new byte[]{0x10, 0x00, 0x04})); // Side 1 -> Seconds 16
        historyEntryListTwo.add(new HistoryEntry(new byte[]{0x10, 0x00, 0x08})); // Side 2 -> Seconds 16

        when(cubeManager.getHistory("0C:61:CF:C7:8F:D5")).thenReturn(historyEntryListOne); // cube 1 history list one
        when(cubeManager.getHistory("0C:61:CF:C7:8F:D6")).thenReturn(historyEntryListTwo); // cube 2 history list two

        return  cubeManager;
    }
}
