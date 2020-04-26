package uibk.ac.at.prodigaclient.BluetoothUtility;

import net.jodah.failsafe.*;
import tinyb.BluetoothDevice;
import tinyb.BluetoothException;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Cube {
    private static final byte[] CUBEPASSWORD = {0x30, 0x30, 0x30, 0x30, 0x30, 0x30};
    private static final byte[] READHISTORYCMD = {0x01};
    private static final byte[] WRITEHISTORYCMD = {0x02};

    private static final String FACETSERVICEUUID = "f1196f50-71a4-11e6-bdf4-0800200c9a66";
    private static final String BATTERYSERVICEUUID = "0000180f-0000-1000-8000-00805f9b34fb";

    private static final String BATTERYCHARACTERISTICUUID = "00002a19-0000-1000-8000-00805f9b34fb";
    private static final String CURRENTFACETCHARACTERISTICUUID = "f1196f52-71a4-11e6-bdf4-0800200c9a66";
    private static final String COMMANDREADCHARACTERISTICUUID = "f1196f53-71a4-11e6-bdf4-0800200c9a66";
    private static final String COMMANDWRITERCHARACTERISTICUUID = "f1196f54-71a4-11e6-bdf4-0800200c9a66";
    private static final String PASSWORDCHARACTERISTICUUID = "f1196f57-71a4-11e6-bdf4-0800200c9a66";

    private BluetoothDevice cube;
    private String name;
    private String address;

    protected Cube(BluetoothDevice cube) {
        this.cube = cube;
        this.name = cube.getName();
        this.address = cube.getAddress();
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    // check if device has the service we need to use.
    public boolean isCube() {
        failsafeConnect();
        BluetoothGattService gattService = getService(FACETSERVICEUUID);
        failsafeDisconnect();
        return (gattService != null);
    }

    private BluetoothGattService getService(String UUID) {
        boolean found = false;

        BluetoothGattService specificBluetoothService = null;

        while (!found) {
            List<BluetoothGattService> bluetoothServices = cube.getServices();
            if (bluetoothServices == null) {
                return null;
            }

            for (BluetoothGattService service : bluetoothServices) {
                if (service.getUUID().equals(UUID)) {
                    specificBluetoothService = service;
                    found = true;
                }
            }
        }

        return specificBluetoothService;
    }

    private BluetoothGattCharacteristic getCharacteristic(BluetoothGattService service, String UUID) {
        boolean found = false;

        BluetoothGattCharacteristic specificBluetoothCharacteristic = null;

        while (!found) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

            if (characteristics == null) {
                return null;
            }

            for (BluetoothGattCharacteristic characteristic : characteristics) {
                if (characteristic.getUUID().equals(UUID)) {
                    specificBluetoothCharacteristic = characteristic;
                    found = true;
                }
            }
        }

        return specificBluetoothCharacteristic;
    }

    private boolean isLast(byte [] test) {
        for (byte x : test) {
            if (x != 0x00) {
                return false;
            }
        }
        return true;
    }

    protected void failsafeConnect() {
        RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
                .handle(BluetoothException.class)
                .withDelay(Duration.ofSeconds(1))
                .withMaxRetries(3);

        if (!cube.getConnected()) {
            Failsafe.with(retryPolicy).run(() -> cube.connect());
        }
    }

    protected void failsafeDisconnect() {
        RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
                .handle(BluetoothException.class)
                .withDelay(Duration.ofSeconds(1))
                .withMaxRetries(3);

        if (cube.getConnected()) {
            Failsafe.with(retryPolicy).run(() -> cube.disconnect());
        }
    }

    private void inputPW(BluetoothGattService facetService) {
        BluetoothGattCharacteristic passwordChar = getCharacteristic(facetService, PASSWORDCHARACTERISTICUUID);
        if (passwordChar != null) {
            passwordChar.writeValue(CUBEPASSWORD);
        } else {
            // TODO: Exception?
            System.out.println("Password characteristic not found");
        }
    }

    private void sendCommand(BluetoothGattService facetService, byte [] command) {
        BluetoothGattCharacteristic commandInputChar = getCharacteristic(facetService, COMMANDWRITERCHARACTERISTICUUID); // command input characteristic
        commandInputChar.writeValue(command);
    }

    private List<HistoryEntry> getHistoryList(List<byte []> historyList) {
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

    public List<HistoryEntry> getHistory() {
        List<HistoryEntry> historyEntryList = null;
        BluetoothGattService facetService = getService(FACETSERVICEUUID); // TimeFlip Service

        if (facetService != null) {
            inputPW(facetService);

            sendCommand(facetService, READHISTORYCMD);

            BluetoothGattCharacteristic commandOutputChar = getCharacteristic(facetService, COMMANDREADCHARACTERISTICUUID); // command output characteristic used to read the history
            List<byte[]> historyList = new LinkedList<>();
            byte[] history = commandOutputChar.readValue();

            while (!isLast(history)) {
                historyList.add(history);
                history = commandOutputChar.readValue();
            }

            historyList.remove(historyList.size() - 1);

            historyEntryList = getHistoryList(historyList);
        } else {
            // TODO: Exception ??
            System.out.println("Facet service not found");
        }

        return historyEntryList;
    }

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

    public int getCurrentSide() { // TODO: Work in progress
        int currentSide = 0;
        BluetoothGattService facetService = getService(FACETSERVICEUUID); // TimeFlip Service
        if (facetService != null) {
            inputPW(facetService);
            BluetoothGattCharacteristic currentFacet = getCharacteristic(facetService, CURRENTFACETCHARACTERISTICUUID);

            byte[] currentFacetHex = currentFacet.readValue();
            currentSide = Byte.toUnsignedInt(currentFacetHex[0]);
        } else {
            // TODO: Exception ??
            System.out.println("Facet service not found");
        }

        return currentSide;
    }
}
