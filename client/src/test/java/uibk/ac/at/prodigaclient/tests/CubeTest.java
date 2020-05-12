package uibk.ac.at.prodigaclient.tests;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;
import uibk.ac.at.prodigaclient.BluetoothUtility.Cube;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//@RunWith(MockitoJUnitRunner.class)
public class CubeTest {
    private static final String DEVICE_MAC = "12:34:56:78:90:12";
    private static final String DEVICE_NAME = "TimeFlip";

    private static final String FACETSERVICEUUID = "f1196f50-71a4-11e6-bdf4-0800200c9a66";
    private static final String BATTERYSERVICEUUID = "0000180f-0000-1000-8000-00805f9b34fb";

    private static final String BATTERYCHARACTERISTICUUID = "00002a19-0000-1000-8000-00805f9b34fb";
    private static final String CURRENTFACETCHARACTERISTICUUID = "f1196f52-71a4-11e6-bdf4-0800200c9a66";
    private static final String COMMANDREADCHARACTERISTICUUID = "f1196f53-71a4-11e6-bdf4-0800200c9a66";
    private static final String COMMANDWRITERCHARACTERISTICUUID = "f1196f54-71a4-11e6-bdf4-0800200c9a66";
    private static final String PASSWORDCHARACTERISTICUUID = "f1196f57-71a4-11e6-bdf4-0800200c9a66";

    private static final String[] FLAGS = {"read", "write-without-response", "notify"};
    private static final boolean NOTIFYING = true;
    private static final byte[] CUBEPASSWORD = {0x30, 0x30, 0x30, 0x30, 0x30, 0x30};
    private static final byte[] READHISTORYCMD = {0x01};
    private static final byte[] DELETEHISTORYCMD = {0x02};

    private BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
    private BluetoothGattService bluetoothBatteryService = mock(BluetoothGattService.class);
    private BluetoothGattService bluetoothFacetService = mock(BluetoothGattService.class);

    private BluetoothGattCharacteristic bluetoothBatteryCharacteristic = mock(BluetoothGattCharacteristic.class);
    private BluetoothGattCharacteristic bluetoothCurrentFacetCharacteristics = mock(BluetoothGattCharacteristic.class);
    private BluetoothGattCharacteristic bluetoothCommandWriteCharacteristics = mock(BluetoothGattCharacteristic.class);
    private BluetoothGattCharacteristic bluetoothCommandReadCharacteristics = mock(BluetoothGattCharacteristic.class);
    private BluetoothGattCharacteristic bluetoothPasswordCharacteristic = mock(BluetoothGattCharacteristic.class);

    private Cube cube;

    @BeforeEach
    public void setUp() {
        when(bluetoothDevice.getName()).thenReturn(DEVICE_NAME);
        when(bluetoothDevice.getAddress()).thenReturn(DEVICE_MAC);

        when(bluetoothDevice.connect()).thenReturn(true);
        when(bluetoothDevice.getConnected()).thenReturn(true);
        when(bluetoothDevice.getConnected()).thenReturn(true);

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

        when(bluetoothBatteryCharacteristic.readValue()).thenReturn(new byte[]{0x45});

        when(bluetoothPasswordCharacteristic.writeValue(CUBEPASSWORD)).thenReturn(true);

        when(bluetoothCommandWriteCharacteristics.writeValue(READHISTORYCMD)).thenReturn(true);
        when(bluetoothCommandWriteCharacteristics.writeValue(DELETEHISTORYCMD)).thenReturn(true);

        when(bluetoothCommandReadCharacteristics.readValue()).thenReturn(flipBetweenHistory());
        when(bluetoothCurrentFacetCharacteristics.readValue()).thenReturn(new byte[]{0x01});

        cube = new Cube(bluetoothDevice);
    }

    private int historyRead = 0;

    private byte[] flipBetweenHistory() {
        byte[] returnValue;
        switch (historyRead){
            case 0:
                historyRead = 1;
                returnValue = new byte[] {0x0d, 0x00, 0x18, 0x3c, 0x00, 0x04, 0x10, 0x00, 0x04, 0x10, 0x00, 0x08,
                        0x16, 0x00, 0x0c, 0x32, 0x00, 0x10, 0x0d, 0x00, 0x14};
                break;
            case 1:
                historyRead = 2;
                returnValue = new byte[] {0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                break;
            case 2:
                historyRead = 0;
                returnValue = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                break;
            default:
                historyRead = 0;
                returnValue = new byte[]{};
                break;
        }

        return returnValue;
    }

    @Test
    public void getCubeNameTest() {
        Assertions.assertEquals(DEVICE_NAME, cube.getName());
    }

    @Test
    public void getCubeAddress() {
        Assertions.assertEquals(DEVICE_MAC, cube.getAddress());
    }

    @Test
    public void getCurrentSideTest() {
        Assertions.assertEquals(1, cube.getCurrentSide());
    }
}
