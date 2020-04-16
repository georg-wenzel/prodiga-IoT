package uibk.ac.at.prodigaclient.BluetoothUtility;

import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;

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
    private static final String CURRENTFACETCHARACTERISTICUUID = "f1196f52-71a4-11e6-bdf4-0800200c9a6";
    private static final String COMMANDREADCHARACTERISTICUUID = "f1196f53-71a4-11e6-bdf4-0800200c9a66";
    private static final String COMMANDWRITERCHARACTERISTICUUID = "f1196f54-71a4-11e6-bdf4-0800200c9a66";
    private static final String PASSWORDCHARACTERISTICUUID = "f1196f57-71a4-11e6-bdf4-0800200c9a66";

    private BluetoothDevice cube;
    private String name;
    private String address;

    public Cube(BluetoothDevice cube) {
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

    public BluetoothGattService getService(String UUID) {
        BluetoothGattService specificBluetoothService = null;
        List<BluetoothGattService> bluetoothServices = cube.getServices();
        if (bluetoothServices == null) {
            return null;
        }

        for (BluetoothGattService service : bluetoothServices) {
            if (service.getUUID().equals(UUID)) {
                specificBluetoothService = service;
            }
        }

        return specificBluetoothService;
    }

    public BluetoothGattCharacteristic getCharacteristic(BluetoothGattService service, String UUID) {
        BluetoothGattCharacteristic specificBluetoothCharacteristic = null;
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

        if (characteristics == null) {
            return null;
        }

        for (BluetoothGattCharacteristic characteristic : characteristics) {
            if (characteristic.getUUID().equals(UUID)) {
                specificBluetoothCharacteristic = characteristic;
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

        cube.connect();

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

        cube.disconnect();

        return historyEntryList;
    }

    public int getBattery() {
        int batteryStatus = 0;
        cube.connect();

        BluetoothGattService batteryService = getService(BATTERYSERVICEUUID); // TimeFlip Service

        if (batteryService != null) {
            BluetoothGattCharacteristic batteryChar = getCharacteristic(batteryService, BATTERYCHARACTERISTICUUID); // command output characteristic used to read the history
            byte[] batteryStatusHex = batteryChar.readValue();
            batteryStatus = Byte.toUnsignedInt(batteryStatusHex[0]);
        } else {
            System.out.println("Facet service not found");
        }

        cube.disconnect();

        return batteryStatus;
    }

}
