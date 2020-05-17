package uibk.ac.at.prodigaclient.BluetoothUtility;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for the creation and reading of the History List form the timeflip cube
 */
public class HistoryListHelper {
	/**
	 * checks if the given byte array is the last byte array of the cube
	 * If the last history line was send from the time flip cube it sends a line consisting of only {0x00}
	 * https://github.com/DI-GROUP/TimeFlip.Docs/blob/master/Hardware/BLE_device_commutication_protocol_v3.0_en.md
	 * @param test The byte array received from the cube
	 * @return Is it the last line?
	 */
	public static boolean isLast(byte [] test) { // public for testing
		for (byte x : test) {
			if (x != 0x00) {
				return false;
			}
		}
		return true;
	}

	/**
	 * converts the list of byte arrays received from the time flip cube to the list of HistoryEntrys.
	 * @param historyList The list of byte array received from the TimeFlip
	 * @return List of HistoryEntrys
	 */
	public static List<HistoryEntry> convertToHistoryList(List<byte []> historyList) { // public for testing
		List<HistoryEntry> historyEntryList = new LinkedList<>();
		for (byte [] historyBlock: historyList) {
			for (int i = 0; i < 7; i++) {
				HistoryEntry historyEntry = new HistoryEntry(Arrays.copyOfRange(historyBlock, 3 * i,
						3 * (i + 1)));
				if (historyEntry.getSeconds() != 0) {
					historyEntryList.add(historyEntry);
				}
			}
		}

		return historyEntryList;
	}
}
