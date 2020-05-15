package uibk.ac.at.prodigaclient.tests.MockCreators;

import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BluetoothManagerMockCreator {

    public static BluetoothManager mockBluetoothManager() {
        BluetoothManager bluetoothManager = mock(BluetoothManager.class);

        when(bluetoothManager.startDiscovery()).thenReturn(true);
        when(bluetoothManager.stopDiscovery()).thenReturn(true);

        List<BluetoothDevice> bluetoothDeviceList = new LinkedList<>();

        List<byte[]> historyList = new LinkedList<>();
        historyList.add(new byte[]{0x0d, 0x00, 0x18, 0x3c, 0x00, 0x04, 0x10, 0x00, 0x04, 0x10, 0x00, 0x08,
                0x16, 0x00, 0x0c, 0x32, 0x00, 0x10, 0x0d, 0x00, 0x14});
        historyList.add(new byte[]{0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        historyList.add(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

        bluetoothDeviceList.add(BluetoothDeviceMockCreator.mockFullBluetoothDevice("12:34:56:78:90:00", "TimeFlipOne", historyList, new byte[]{0x01}, new byte[] {0x14}, false));
        bluetoothDeviceList.add(BluetoothDeviceMockCreator.mockFullBluetoothDevice("12:34:56:78:90:01", "TimeFlipTwo", historyList, new byte[]{0x02}, new byte[] {0x25}, false));
        bluetoothDeviceList.add(BluetoothDeviceMockCreator.mockFullBluetoothDevice("12:34:56:78:90:02", "notACube", null, null, null, false));

        when(bluetoothManager.getDevices()).thenReturn(bluetoothDeviceList);

        return bluetoothManager;
    }

    public static BluetoothManager mockBluetoothManagerConnectionTest(Boolean connection) {
        BluetoothManager bluetoothManager = mock(BluetoothManager.class);

        when(bluetoothManager.startDiscovery()).thenReturn(true);
        when(bluetoothManager.stopDiscovery()).thenReturn(true);

        List<BluetoothDevice> bluetoothDeviceList = new LinkedList<>();
        bluetoothDeviceList.add(BluetoothDeviceMockCreator.mockConnectionTestBluetoothDevice(connection));

        when(bluetoothManager.getDevices()).thenReturn(bluetoothDeviceList);

        return bluetoothManager;
    }
}
