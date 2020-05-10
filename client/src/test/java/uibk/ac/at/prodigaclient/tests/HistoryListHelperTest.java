package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryListHelper;

import java.util.LinkedList;
import java.util.List;

public class HistoryListHelperTest {
    @Test
    void historyIsLastTest() {
        byte [] byteArray = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        Assertions.assertTrue(HistoryListHelper.isLast(byteArray));

        Assertions.assertTrue(HistoryListHelper.isLast(new byte[] {0x00}));
    }

    @Test
    void historyIsNotLastTest() {
        byte [] byteArray = new byte[] {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        Assertions.assertFalse(HistoryListHelper.isLast(byteArray));

        Assertions.assertFalse(HistoryListHelper.isLast(new byte[] {0x01}));
    }

    @Test
    void convertToHistoryListSizeOfReturnValueTest() {
        byte [] byteArrayOne = new byte[] {0x0d, 0x00, 0x18, 0x3c, 0x00, 0x04, 0x10, 0x00, 0x04, 0x10, 0x00, 0x08,
                0x16, 0x00, 0x0c, 0x32, 0x00, 0x10, 0x0d, 0x00, 0x14};
        byte [] byteArrayTwo = new byte[] {0x0d, 0x00, 0x18, 0x3c, 0x00, 0x04, 0x10, 0x00, 0x04, 0x10, 0x00, 0x08,
                0x16, 0x00, 0x0c, 0x32, 0x00, 0x10, 0x0d, 0x00, 0x14};

        List<byte []> byteList = new LinkedList<>();
        byteList.add(byteArrayOne);
        byteList.add(byteArrayTwo);

        Assertions.assertNotNull(HistoryListHelper.convertToHistoryList(byteList));
        Assertions.assertEquals(14, HistoryListHelper.convertToHistoryList(byteList).size());
    }

    @Test
    void convertToHistoryListSizeOfReturnValueWhenContainingZeroBytesTest() {
        byte [] byteArrayOne = new byte[] {0x0d, 0x00, 0x18, 0x3c, 0x00, 0x04, 0x10, 0x00, 0x04, 0x10, 0x00, 0x08,
                0x16, 0x00, 0x0c, 0x32, 0x00, 0x10, 0x0d, 0x00, 0x14};
        byte [] byteArrayTwo = new byte[] {0x0d, 0x00, 0x18, 0x3c, 0x00, 0x04, 0x10, 0x00, 0x04, 0x00, 0x00, 0x08,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        List<byte []> byteList = new LinkedList<>();
        byteList.add(byteArrayOne);
        byteList.add(byteArrayTwo);

        Assertions.assertNotNull(HistoryListHelper.convertToHistoryList(byteList));
        Assertions.assertEquals(10, HistoryListHelper.convertToHistoryList(byteList).size());
    }

    @Test
    void convertToHistoryListCreationTest() {
        byte [] byteArrayOne = new byte[] {0x0d, 0x00, 0x18, 0x3c, 0x00, 0x04, 0x10, 0x00, 0x04, 0x10, 0x00, 0x08,
                0x16, 0x00, 0x0c, 0x32, 0x00, 0x10, 0x0d, 0x00, 0x14};

        List<byte []> byteList = new LinkedList<>();
        byteList.add(byteArrayOne);
        List<HistoryEntry> historyEntries = HistoryListHelper.convertToHistoryList(byteList);

        List<HistoryEntry> manualCalculatedHistoryEntries = new LinkedList<>();

        manualCalculatedHistoryEntries.add(new HistoryEntry(new byte[]{0x0d, 0x00, 0x18}));
        manualCalculatedHistoryEntries.add(new HistoryEntry(new byte[]{0x3c, 0x00, 0x04}));
        manualCalculatedHistoryEntries.add(new HistoryEntry(new byte[]{0x10, 0x00, 0x04}));
        manualCalculatedHistoryEntries.add(new HistoryEntry(new byte[]{0x10, 0x00, 0x08}));
        manualCalculatedHistoryEntries.add(new HistoryEntry(new byte[]{0x16, 0x00, 0x0c}));
        manualCalculatedHistoryEntries.add(new HistoryEntry(new byte[]{0x32, 0x00, 0x10}));
        manualCalculatedHistoryEntries.add(new HistoryEntry(new byte[]{0x0d, 0x00, 0x14}));

        for (int i = 0; i < byteList.size(); i++) {
            HistoryEntry historyEntry = historyEntries.get(i);
            HistoryEntry historyEntryTest = manualCalculatedHistoryEntries.get(i);

            Assertions.assertEquals(historyEntryTest.getID(), historyEntry.getID());
            Assertions.assertEquals(historyEntryTest.getSeconds(), historyEntry.getSeconds());
        }
    }
}
