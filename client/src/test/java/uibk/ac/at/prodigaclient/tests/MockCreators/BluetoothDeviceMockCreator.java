package uibk.ac.at.prodigaclient.tests.MockCreators;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import tinyb.BluetoothDevice;
import tinyb.BluetoothException;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;
import uibk.ac.at.prodigaclient.BluetoothUtility.TimeFlipProperties;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BluetoothDeviceMockCreator {

    public static BluetoothDevice mockFullBluetoothDevice(String deviceMac, String deviceName, List<byte[]> historyEntries, byte[] facetId, byte[] batteryStatus, boolean connected) {
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

        when(bluetoothDevice.connect()).thenReturn(true);
        when(bluetoothDevice.getConnected()).thenReturn(connected);
        when(bluetoothDevice.disconnect()).thenReturn(true);

        when(bluetoothDevice.find(eq(TimeFlipProperties.BATTERYSERVICEUUID), any(Duration.class))).thenReturn(bluetoothBatteryService);
        when(bluetoothDevice.find(eq(TimeFlipProperties.FACETSERVICEUUID), any(Duration.class))).thenReturn(bluetoothFacetService);

        when(bluetoothBatteryService.getUUID()).thenReturn(TimeFlipProperties.BATTERYSERVICEUUID);
        when(bluetoothFacetService.getUUID()).thenReturn(TimeFlipProperties.FACETSERVICEUUID);

        when(bluetoothFacetService.find(eq(TimeFlipProperties.COMMANDREADCHARACTERISTICUUID), any(Duration.class))).thenReturn(bluetoothCommandReadCharacteristics);
        when(bluetoothFacetService.find(eq(TimeFlipProperties.COMMANDWRITERCHARACTERISTICUUID), any(Duration.class))).thenReturn(bluetoothCommandWriteCharacteristics);
        when(bluetoothFacetService.find(eq(TimeFlipProperties.CURRENTFACETCHARACTERISTICUUID), any(Duration.class))).thenReturn(bluetoothCurrentFacetCharacteristics);
        when(bluetoothFacetService.find(eq(TimeFlipProperties.PASSWORDCHARACTERISTICUUID), any(Duration.class))).thenReturn(bluetoothPasswordCharacteristic);

        when(bluetoothBatteryService.find(eq(TimeFlipProperties.BATTERYCHARACTERISTICUUID), any(Duration.class))).thenReturn(bluetoothBatteryCharacteristic);

        when(bluetoothCommandReadCharacteristics.getUUID()).thenReturn(TimeFlipProperties.COMMANDREADCHARACTERISTICUUID);
        when(bluetoothCommandWriteCharacteristics.getUUID()).thenReturn(TimeFlipProperties.COMMANDWRITERCHARACTERISTICUUID);
        when(bluetoothCurrentFacetCharacteristics.getUUID()).thenReturn(TimeFlipProperties.CURRENTFACETCHARACTERISTICUUID);
        when(bluetoothPasswordCharacteristic.getUUID()).thenReturn(TimeFlipProperties.PASSWORDCHARACTERISTICUUID);
        when(bluetoothBatteryCharacteristic.getUUID()).thenReturn(TimeFlipProperties.BATTERYCHARACTERISTICUUID);

        when(bluetoothBatteryCharacteristic.readValue()).thenReturn(batteryStatus);

        when(bluetoothPasswordCharacteristic.writeValue(any(byte[].class))).thenAnswer(
                (Answer<Boolean>) invocationOnMock -> invocationOnMock.getArgument(0).equals(TimeFlipProperties.CUBEPASSWORD)
        );

        when(bluetoothCommandWriteCharacteristics.writeValue(TimeFlipProperties.READHISTORYCMD)).thenReturn(true);
        when(bluetoothCommandWriteCharacteristics.writeValue(TimeFlipProperties.DELETEHISTORYCMD)).thenReturn(true);

        when(bluetoothCommandReadCharacteristics.readValue()).thenAnswer(new Answer<byte[]>() {
            int historyStatus = 0;
            final List<byte[]> historyEntryList = historyEntries;

            @Override
            public byte[] answer(InvocationOnMock invocationOnMock) {
                byte[] returnValue;
                returnValue = historyEntryList.get(historyStatus % historyEntryList.size());
                historyStatus = historyStatus + 1;

                return returnValue;
            }
        });

        when(bluetoothCurrentFacetCharacteristics.readValue()).thenReturn(facetId);
        return bluetoothDevice;
    }


    public static BluetoothDevice mockConnectionTestBluetoothDevice(boolean connectionStatus) {
        BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);

        when(bluetoothDevice.getName()).thenReturn("TimeFlip");
        when(bluetoothDevice.getAddress()).thenReturn("12:34:56:78:90:12");

        when(bluetoothDevice.connect()).thenReturn(true);
        when(bluetoothDevice.getConnected()).thenReturn(connectionStatus);
        when(bluetoothDevice.disconnect()).thenReturn(true);

        return bluetoothDevice;
    }

    public static BluetoothDevice mockBrokenConnectionTestBluetoothDevice(boolean connectionStatus) {
        BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);

        when(bluetoothDevice.getName()).thenReturn("TimeFlip");
        when(bluetoothDevice.getAddress()).thenReturn("12:34:56:78:90:12");

        when(bluetoothDevice.connect()).thenThrow(BluetoothException.class).thenThrow(BluetoothException.class).thenReturn(true);
        when(bluetoothDevice.getConnected()).thenReturn(connectionStatus);
        when(bluetoothDevice.disconnect()).thenThrow(BluetoothException.class).thenThrow(BluetoothException.class).thenReturn(true);

        return bluetoothDevice;
    }
}
