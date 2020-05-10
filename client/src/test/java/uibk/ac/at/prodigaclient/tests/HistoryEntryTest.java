package uibk.ac.at.prodigaclient.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;

public class HistoryEntryTest {

    @Test
    void creationTest() {
        HistoryEntry historyEntry = new HistoryEntry(new byte[]{0x0d, 0x00, 0x18});
        Assertions.assertNotNull(historyEntry);
    }

    @Test
    void secondsTest() {
        HistoryEntry historyEntry = new HistoryEntry(new byte[]{0x0d, 0x00, 0x18});
        assertEquals(13, historyEntry.getSeconds());

        historyEntry = new HistoryEntry(new byte[]{0x0d, 0x01, 0x00});
        assertEquals(269, historyEntry.getSeconds());

        historyEntry = new HistoryEntry(new byte[]{0x0d, 0x11, 0x00});
        assertEquals(4365, historyEntry.getSeconds());

        historyEntry = new HistoryEntry(new byte[]{0x00, 0x00, 0x05});
        assertEquals(65536, historyEntry.getSeconds());
    }

    @Test
    void idTest() {
        HistoryEntry historyEntry = new HistoryEntry(new byte[]{0x0d, 0x00, 0x18});
        assertEquals(6, historyEntry.getID());

        historyEntry = new HistoryEntry(new byte[]{0x00, 0x00, 0x04});
        assertEquals(1, historyEntry.getID());

        historyEntry = new HistoryEntry(new byte[]{0x00, 0x00, 0x08});
        assertEquals(2, historyEntry.getID());

        historyEntry = new HistoryEntry(new byte[]{0x00, 0x00, 0x14});
        assertEquals(5, historyEntry.getID());

        historyEntry = new HistoryEntry(new byte[]{0x00, 0x00, 0x44});
        assertEquals(17, historyEntry.getID());
    }

    @Test
    void zeroSecondsTest() {
        HistoryEntry historyEntry = new HistoryEntry(new byte[]{0x00, 0x00, 0x00});
        assertEquals(0, historyEntry.getSeconds());
    }

    @Test
    void zeroIdTest() {
        HistoryEntry historyEntry = new HistoryEntry(new byte[]{0x00, 0x00, 0x00});
        assertEquals(0, historyEntry.getID());
    }


    @Test
    void maxSecondsTest() {
        HistoryEntry historyEntry = new HistoryEntry(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff});
        assertEquals(262143, historyEntry.getSeconds());
    }

    @Test
    void maxIdTest() {
        HistoryEntry historyEntry = new HistoryEntry(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff});
        assertEquals(63, historyEntry.getID());
    }
}
