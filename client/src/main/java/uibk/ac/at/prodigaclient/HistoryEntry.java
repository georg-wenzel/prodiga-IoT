package uibk.ac.at.prodigaclient;

public class HistoryEntry {
    private int ID;
    private int seconds;

    HistoryEntry(byte[] entry) {
        this.ID = getFacet(entry);
        this.seconds = this.getTime(entry);
    }

    public int getID() {
        return ID;
    }

    public int getSeconds() {
        return seconds;
    }

    public boolean addEntrySecondsTogether(HistoryEntry historyEntry) {
        if (historyEntry.getID() == this.ID) {
             this.seconds += historyEntry.getSeconds();
             return true;
        } else {
            return false;
        }
    }

    // facets decoding
    private int getFacet(byte [] byteArray) {
        return byteArray[2] >> 2;
    }

    // little endian conversion
    private int getTime(byte [] byteArray) {
        return ((Byte.toUnsignedInt(byteArray[2]) & 0x03) << 16) | (Byte.toUnsignedInt(byteArray[1]) << 8)
                | (Byte.toUnsignedInt(byteArray[0]));
    }

    @Override
    public String toString() {
        return "Facet ID: " + ID + " -> Seconds: " + seconds;
    }
}
