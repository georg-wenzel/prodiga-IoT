package uibk.ac.at.prodigaclient.BluetoothUtility;

/**
 * Represents a history entry
 */
public class HistoryEntry {
    /**
     * ID of the side
     */
    private int ID;

    /**
     * total seconds the cube lied on the side
     */
    private int seconds;

    /**
     * Constructs the history entry
     * @param entry a 3 wide byte array which we get from the cube
     */
    public HistoryEntry(byte[] entry) {
        this.ID = getFacet(entry);
        this.seconds = this.getTime(entry);
    }

    /**
     * Get the id from the side
     * @return side id
     */
    public int getID() {
        return ID;
    }

    /**
     * Get the seconds
     * @return time in seconds
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * add two entrys together if they have the same id
     * @param historyEntry second history entry
     * @return was it successful
     */
    public boolean addEntrySecondsTogether(HistoryEntry historyEntry) {
        if (historyEntry.getID() == this.ID) {
             this.seconds += historyEntry.getSeconds();
             return true;
        } else {
            return false;
        }
    }

    /**
     * get the facet id
     * @param byteArray three wide byte array form the cube
     * @return facet id
     */
    private int getFacet(byte [] byteArray) {
        return Byte.toUnsignedInt(byteArray[2]) >> 2;
    }

    /**
     * get the total time
     * @param byteArray three wide byte array form the cube
     * @return total time in seconds
     */
    private int getTime(byte [] byteArray) {
        return ((Byte.toUnsignedInt(byteArray[2]) & 0x03) << 16) | (Byte.toUnsignedInt(byteArray[1]) << 8)
                | (Byte.toUnsignedInt(byteArray[0]));
    }

    @Override
    public String toString() {
        return "Facet ID: " + ID + " -> Seconds: " + seconds;
    }
}
