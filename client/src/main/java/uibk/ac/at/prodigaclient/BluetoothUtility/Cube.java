package uibk.ac.at.prodigaclient.BluetoothUtility;

import net.jodah.failsafe.*;
import tinyb.BluetoothDevice;
import tinyb.BluetoothException;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is represents an internal cube. It is a wrapper for the BluetoothDevice class.
 */
public class Cube {
    private static final byte[] CUBEPASSWORD = {0x30, 0x30, 0x30, 0x30, 0x30, 0x30};
    private static final byte[] READHISTORYCMD = {0x01};
    private static final byte[] DELETEHISTORYCMD = {0x02};

    private static final String FACETSERVICEUUID = "f1196f50-71a4-11e6-bdf4-0800200c9a66";
    private static final String BATTERYSERVICEUUID = "0000180f-0000-1000-8000-00805f9b34fb";

    private static final String BATTERYCHARACTERISTICUUID = "00002a19-0000-1000-8000-00805f9b34fb";
    private static final String CURRENTFACETCHARACTERISTICUUID = "f1196f52-71a4-11e6-bdf4-0800200c9a66";
    private static final String COMMANDREADCHARACTERISTICUUID = "f1196f53-71a4-11e6-bdf4-0800200c9a66";
    private static final String COMMANDWRITERCHARACTERISTICUUID = "f1196f54-71a4-11e6-bdf4-0800200c9a66";
    private static final String PASSWORDCHARACTERISTICUUID = "f1196f57-71a4-11e6-bdf4-0800200c9a66";

    /**
     * internal bluetooth device
     */
    private BluetoothDevice cube;

    /**
     * Bluetooth Device Name
     */
    private String name;

    /**
     * Bluetooth Device Address
     */
    private String address;

    /**
     * The facet service for the cube
     * <p>
     *      This is the facet service most of the functions need. It should be connected to only one time during
     *      the the connection read out period. For this it will be "lazy" loaded when we need it.
     * </p>
     */
    private BluetoothGattService facetService = null;

    /**
     * Creates a instance of the Cube.
     * It initializes name, address and cube
     * @param cube the BluetoothDevice which this Cube class represents
     */
    public Cube(BluetoothDevice cube) {
        this.cube = cube;
        this.name = cube.getName();
        this.address = cube.getAddress();
    }

    /**
     * Gets the name of the Cube. This is used to identify the Bluetooth device as a Cube
     * @return Name from the BluetoothDevice cube.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the Mac Address of the cube. This is used to identify the cube.
     * @return Address from the BluetoothDevice cube.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Initialisation of the cube. This includes to get the facet service and input the password
     * @return Successfully initialised?
     */
    private boolean initializeCube() {
        if (facetService == null) {
            facetService = getService(FACETSERVICEUUID);

            inputPW();

            return (facetService != null);
        } else {
            return true;
        }
    }

    /**
     * Make a failsafe connection to the bluetooth Device
     */
    public void failsafeConnect() {
        if (!cube.getConnected()) {
            RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
                    .handle(BluetoothException.class)
                    .withDelay(Duration.ofSeconds(1))
                    .withMaxRetries(10);

            Failsafe.with(retryPolicy).run(cube::connect);
        }
    }

    /**
     * Make a failsafe disconnection from the bluetooth Device
     */
    public void failsafeDisconnect() {
        if (cube.getConnected()) {
            RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
                    .handle(BluetoothException.class)
                    .withDelay(Duration.ofSeconds(1))
                    .withMaxRetries(10);

            facetService = null;
            Failsafe.with(retryPolicy).run(cube::disconnect);
        }
    }

    /**
     * Inputs the password to a Time Flip cube.
     * Uses the password specified previously
     */
    private void inputPW() {
        BluetoothGattCharacteristic passwordChar = getCharacteristic(facetService, PASSWORDCHARACTERISTICUUID);
        if (passwordChar != null) {
            passwordChar.writeValue(CUBEPASSWORD);
        } else {
            System.out.println("Password characteristic not found");
        }
    }

    /**
     * Gets a specific service we need form the service list of the Bluetooth Device
     * @param UUID The service identifier
     * @return Specific service
     */
    private BluetoothGattService getService(String UUID) {
        return cube.find(UUID, Duration.ofSeconds(10));
    }

    /**
     * Gets a specific characteristic we need form the characteristics list of the specified service
     * @param service The bluetooth service from which we need the characteristic from
     * @param UUID The characteristic identifier
     * @return Specific characteristic
     */
    private BluetoothGattCharacteristic getCharacteristic(BluetoothGattService service, String UUID) {
        return service.find(UUID, Duration.ofSeconds(10));
    }

    /**
     * Send a command to the cubes command characteristics.
     * Some of the possible commands can be inspected here:
     * https://github.com/DI-GROUP/TimeFlip.Docs/blob/master/Hardware/BLE_device_commutication_protocol_v3.0_en.md
     * @param command the command we want to send
     */
    private void sendCommand(byte [] command) {
        BluetoothGattCharacteristic commandInputChar = getCharacteristic(facetService, COMMANDWRITERCHARACTERISTICUUID); // command input characteristic
        commandInputChar.writeValue(command);
    }

    /**
     * Reads out the history of the cube
     * @return A list of HistoryEntrys we got from the cube
     */
    public List<HistoryEntry> getHistory() {
        List<HistoryEntry> historyEntryList = null;

        if (initializeCube()) {
            sendCommand(READHISTORYCMD);

            BluetoothGattCharacteristic commandOutputChar = getCharacteristic(facetService, COMMANDREADCHARACTERISTICUUID); // command output characteristic used to read the history
            List<byte[]> historyList = new LinkedList<>();
            byte[] history = commandOutputChar.readValue();

            while (!HistoryListHelper.isLast(history)) {
                historyList.add(history);
                history = commandOutputChar.readValue();
            }

            historyList.remove(historyList.size() - 1);

            historyEntryList = HistoryListHelper.convertToHistoryList(historyList);
        } else {
            System.out.println("Facet service not found");
        }

        return historyEntryList;
    }

    /**
     * Deletes the history of the cube.
     */
    public void deleteHistory() {
        if (initializeCube()) {
            sendCommand(DELETEHISTORYCMD);
        } else {
            System.out.println("Facet service not found");
        }
    }


    /**
     * get the current side of the cube.
     * @return id of the current side
     */
    public int getCurrentSide() {
        int currentSide = 0;
        if (initializeCube()) {
            BluetoothGattCharacteristic currentFacet = getCharacteristic(facetService, CURRENTFACETCHARACTERISTICUUID);

            byte[] currentFacetHex = currentFacet.readValue();
            currentSide = Byte.toUnsignedInt(currentFacetHex[0]);
        } else {
            System.out.println("Facet service not found");
        }

        return currentSide;
    }

    /**
     * get the current battery status.
     * @return batterystatus in %
     */
    public int getBattery() {
        int batteryStatus = 0;
        BluetoothGattService batteryService = getService(BATTERYSERVICEUUID); // TimeFlip Service

        if (batteryService != null) {
            BluetoothGattCharacteristic batteryChar = getCharacteristic(batteryService, BATTERYCHARACTERISTICUUID); // command output characteristic used to read the history
            byte[] batteryStatusHex = batteryChar.readValue();
            batteryStatus = Byte.toUnsignedInt(batteryStatusHex[0]);
        } else {
            System.out.println("Battery service not found");
        }

        return batteryStatus;
    }
}
