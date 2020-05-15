package uibk.ac.at.prodigaclient.tests.MockCreators;

import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BluetoothManagerMockCreator {
    // TODO: improve this boy here
    public static BluetoothManager mockBluetoothManager(List<BluetoothDevice> bluetoothDeviceList) {
        BluetoothManager bluetoothManager = mock(BluetoothManager.class);

        when(bluetoothManager.startDiscovery()).thenReturn(true);
        when(bluetoothManager.stopDiscovery()).thenReturn(true);

        when(bluetoothManager.getDevices()).thenReturn(bluetoothDeviceList);

        return bluetoothManager;
    }
}
