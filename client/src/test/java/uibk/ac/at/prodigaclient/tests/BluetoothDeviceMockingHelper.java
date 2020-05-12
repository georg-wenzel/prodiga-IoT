package uibk.ac.at.prodigaclient.tests;

import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BluetoothDeviceMockingHelper {
    private static final String DEVICE_MAC = "12:34:56:78:90:12";
    private static final String DEVICE_NAME = "TimeFlip";

    private static final String FACETSERVICEUUID = "f1196f50-71a4-11e6-bdf4-0800200c9a66";
    private static final String BATTERYSERVICEUUID = "0000180f-0000-1000-8000-00805f9b34fb";

    private static final String BATTERYCHARACTERISTICUUID = "00002a19-0000-1000-8000-00805f9b34fb";
    private static final String CURRENTFACETCHARACTERISTICUUID = "f1196f52-71a4-11e6-bdf4-0800200c9a66";
    private static final String COMMANDREADCHARACTERISTICUUID = "f1196f53-71a4-11e6-bdf4-0800200c9a66";
    private static final String COMMANDWRITERCHARACTERISTICUUID = "f1196f54-71a4-11e6-bdf4-0800200c9a66";
    private static final String PASSWORDCHARACTERISTICUUID = "f1196f57-71a4-11e6-bdf4-0800200c9a66";

    private static final byte[] CUBEPASSWORD = {0x30, 0x30, 0x30, 0x30, 0x30, 0x30};
    private static final byte[] READHISTORYCMD = {0x01};
    private static final byte[] DELETEHISTORYCMD = {0x02};

    public static BluetoothDevice mockBluetoothDevice() {
        BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        BluetoothGattService bluetoothBatteryService = mock(BluetoothGattService.class);
        BluetoothGattService bluetoothFacetService = mock(BluetoothGattService.class);

        BluetoothGattCharacteristic bluetoothBatteryCharacteristic = mock(BluetoothGattCharacteristic.class);
        BluetoothGattCharacteristic bluetoothCurrentFacetCharacteristics = mock(BluetoothGattCharacteristic.class);
        BluetoothGattCharacteristic bluetoothCommandWriteCharacteristics = mock(BluetoothGattCharacteristic.class);
        BluetoothGattCharacteristic bluetoothCommandReadCharacteristics = mock(BluetoothGattCharacteristic.class);
        BluetoothGattCharacteristic bluetoothPasswordCharacteristic = mock(BluetoothGattCharacteristic.class);

        when(bluetoothDevice.getName()).thenReturn(DEVICE_NAME);
        when(bluetoothDevice.getAddress()).thenReturn(DEVICE_MAC);

        when(bluetoothDevice.connect()).thenReturn(connectToCube());
        when(bluetoothDevice.getConnected()).thenReturn(connected);
        when(bluetoothDevice.disconnect()).thenReturn(disconnectFromCube());

        List<BluetoothGattService> serviceList = new LinkedList<>();
        serviceList.add(bluetoothBatteryService);
        serviceList.add(bluetoothFacetService);

        when(bluetoothDevice.getServices()).thenReturn(serviceList);

        when(bluetoothBatteryService.getUUID()).thenReturn(BATTERYSERVICEUUID);
        when(bluetoothFacetService.getUUID()).thenReturn(FACETSERVICEUUID);

        List<BluetoothGattCharacteristic> characteristicListOne = new LinkedList<>();
        characteristicListOne.add(bluetoothCommandReadCharacteristics);
        characteristicListOne.add(bluetoothCommandWriteCharacteristics);
        characteristicListOne.add(bluetoothCurrentFacetCharacteristics);
        characteristicListOne.add(bluetoothPasswordCharacteristic);

        when(bluetoothFacetService.getCharacteristics()).thenReturn(characteristicListOne);

        List<BluetoothGattCharacteristic> characteristicListTwo = new LinkedList<>();
        characteristicListOne.add(bluetoothBatteryCharacteristic);

        when(bluetoothBatteryService.getCharacteristics()).thenReturn(characteristicListTwo);

        when(bluetoothCommandReadCharacteristics.getUUID()).thenReturn(COMMANDREADCHARACTERISTICUUID);
        when(bluetoothCommandWriteCharacteristics.getUUID()).thenReturn(COMMANDWRITERCHARACTERISTICUUID);
        when(bluetoothCurrentFacetCharacteristics.getUUID()).thenReturn(CURRENTFACETCHARACTERISTICUUID);
        when(bluetoothPasswordCharacteristic.getUUID()).thenReturn(PASSWORDCHARACTERISTICUUID);
        when(bluetoothBatteryCharacteristic.getUUID()).thenReturn(BATTERYCHARACTERISTICUUID);

        when(bluetoothBatteryCharacteristic.readValue()).thenReturn(new byte[]{0x45}); // you can always get the Bluetooth percentage

        when(bluetoothPasswordCharacteristic.writeValue(CUBEPASSWORD)).thenReturn(insertPassword());

        when(bluetoothCommandWriteCharacteristics.writeValue(READHISTORYCMD)).thenReturn(true);
        when(bluetoothCommandWriteCharacteristics.writeValue(DELETEHISTORYCMD)).thenReturn(true);

        when(bluetoothCommandReadCharacteristics.readValue()).thenReturn(flipBetweenHistory());
        when(bluetoothCurrentFacetCharacteristics.readValue()).thenReturn(getCurrentFacet());
        return bluetoothDevice;
    }

    private static int historyRead = -1;
    private static boolean hasInseartedPW = false;
    private static boolean connected = false;

    private static boolean insertPassword() {
        historyRead = 0;
        hasInseartedPW = true;
        return true;
    }

    private static boolean connectToCube() {
        hasInseartedPW = false;
        connected = true;
        return true;
    }

    private static boolean disconnectFromCube() {
        hasInseartedPW = false;
        connected = false;
        return true;
    }

    private static byte[] getCurrentFacet() {
        byte[] returnValue;

        if (hasInseartedPW) {
            returnValue = new byte[]{0x01};
        } else {
            returnValue = new byte[]{};
        }

        return returnValue;
    }

    private static byte[] flipBetweenHistory() {
        byte[] returnValue;

        if(hasInseartedPW) {
            switch (historyRead) {
                case 0:
                    historyRead = 1;
                    returnValue = new byte[]{0x0d, 0x00, 0x18, 0x3c, 0x00, 0x04, 0x10, 0x00, 0x04, 0x10, 0x00, 0x08,
                            0x16, 0x00, 0x0c, 0x32, 0x00, 0x10, 0x0d, 0x00, 0x14};
                    break;
                case 1:
                    historyRead = 2;
                    returnValue = new byte[]{0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    break;
                case 2:
                    historyRead = 0;
                    returnValue = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    break;
                default:
                    returnValue = new byte[]{};
                    break;
            }
        } else {
            returnValue = new byte[]{};
        }

        return returnValue;
    }
}
