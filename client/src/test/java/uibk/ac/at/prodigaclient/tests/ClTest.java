package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.Test;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClTest {
    @Test
    void test_tests() {
        assertEquals(2, 2);
    }

    @Test
    void test_history_entry() {
        byte [] array = {0x0d, 0x00, 0x18};
        HistoryEntry historyEntry = new HistoryEntry(array);

        assertEquals(6, historyEntry.getID());
        assertEquals(13, historyEntry.getSeconds());
    }
}
