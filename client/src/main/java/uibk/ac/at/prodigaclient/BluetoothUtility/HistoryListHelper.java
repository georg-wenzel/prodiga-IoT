package uibk.ac.at.prodigaclient.BluetoothUtility;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class HistoryListHelper {
	protected static boolean isLast(byte [] test) {
		for (byte x : test) {
			if (x != 0x00) {
				return false;
			}
		}
		return true;
	}

	protected static List<HistoryEntry> convertToHistoryList(List<byte []> historyList) {
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
