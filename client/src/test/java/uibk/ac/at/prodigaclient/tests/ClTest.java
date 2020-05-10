package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;

// this class was here to test if everything works for testing the client.
public class ClTest {
    @Test
    void test_tests() {
        Assertions.assertEquals(2, 2);
    }

    @Test
    void test_history_entry() {
        byte [] array = {0x0d, 0x00, 0x18};
        HistoryEntry historyEntry = new HistoryEntry(array);

        Assertions.assertEquals(6, historyEntry.getID());
        Assertions.assertEquals(13, historyEntry.getSeconds());
    }
}
