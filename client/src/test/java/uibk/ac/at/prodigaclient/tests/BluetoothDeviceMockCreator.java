package uibk.ac.at.prodigaclient.tests;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BluetoothDeviceMockCreator {
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

    private String deviceMac;
    private String deviceName;

    private List<byte[]> historyEntries;
    private byte[] facetId;
    private byte[] noPass = new byte[] {};

    private boolean hasInseartedPW = false;
    private boolean connected = false;
    private int historyStatus = 0;

    public BluetoothDeviceMockCreator (String deviceMac, String deviceName, List<byte[]> historyEntries, byte[] facetId) {
        this.deviceMac = deviceMac;
        this.deviceName = deviceName;
        this.historyEntries = historyEntries;
        this.facetId = facetId;
    }

    public BluetoothDevice mockBluetoothDevice() {
        BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        BluetoothGattService bluetoothBatteryService = mock(BluetoothGattService.class);
        BluetoothGattService bluetoothFacetService = mock(BluetoothGattService.class);

        BluetoothGattCharacteristic bluetoothBatteryCharacteristic = mock(BluetoothGattCharacteristic.class);
        BluetoothGattCharacteristic bluetoothCurrentFacetCharacteristics = mock(BluetoothGattCharacteristic.class);
        BluetoothGattCharacteristic bluetoothCommandWriteCharacteristics = mock(BluetoothGattCharacteristic.class);
        BluetoothGattCharacteristic bluetoothCommandReadCharacteristics = mock(BluetoothGattCharacteristic.class);
        BluetoothGattCharacteristic bluetoothPasswordCharacteristic = mock(BluetoothGattCharacteristic.class);

        when(bluetoothDevice.getName()).thenReturn(deviceName);
        when(bluetoothDevice.getAddress()).thenReturn(deviceMac);

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

        when(bluetoothCommandReadCharacteristics.readValue()).thenReturn(getHistory());
//        when(bluetoothCommandReadCharacteristics.readValue()).thenAnswer(new Answer<byte[]>() {
//            @Override
//            public byte[] answer(InvocationOnMock invocationOnMock) throws Throwable {
//
//            }
//        });
        when(bluetoothCurrentFacetCharacteristics.readValue()).thenReturn(getCurrentFacet());
        return bluetoothDevice;
    }


    private byte[] getHistory() {
        byte[] returnValue;

        if(hasInseartedPW) {
            returnValue = historyEntries.get(historyStatus % historyEntries.size());
            historyStatus = historyStatus + 1;
        } else {
            returnValue = noPass;
        }

        return returnValue;
    }

    private boolean insertPassword() {
        hasInseartedPW = true;
        return true;
    }

    private boolean connectToCube() {
        hasInseartedPW = false;
        connected = true;
        return true;
    }

    private boolean disconnectFromCube() {
        hasInseartedPW = false;
        connected = false;
        return true;
    }

    private byte[] getCurrentFacet() {
        byte[] returnValue;

        if (hasInseartedPW) {
            returnValue = facetId;
        } else {
            returnValue = noPass;
        }

        return returnValue;
    }
}
