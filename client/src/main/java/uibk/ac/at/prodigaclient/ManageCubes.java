package uibk.ac.at.prodigaclient;

import tinyb.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ManageCubes {
    BluetoothManager manager;
    HashMap<String, BluetoothDevice> listOfCubes;
    boolean discoveryStarted;

    public ManageCubes () {
        manager = BluetoothManager.getBluetoothManager();
        listOfCubes = new HashMap<>();
        discoveryStarted = manager.startDiscovery();
    }

    public void updateDeviceList() {
        List<BluetoothDevice> list = manager.getDevices();

        if (list != null) {
            // get a list of all current Cubes in the area
            // TODO: test if all devices are shown instantly or we have to add a delay
            listOfCubes = (HashMap<String, BluetoothDevice>) list.stream().filter(x -> x.getName().toLowerCase()
                    .contains("timeflip")).collect(Collectors.toMap(BluetoothDevice::getName, Function.identity()));
        }
    }

     public BluetoothGattService getService(BluetoothDevice device, String UUID) throws InterruptedException {
        BluetoothGattService specificBluetoothService = null;
        List<BluetoothGattService> bluetoothServices = null;

        do {
            bluetoothServices = device.getServices();
            if (bluetoothServices == null) {
                return null;
            }

            for (BluetoothGattService service : bluetoothServices) {
                if (service.getUUID().equals(UUID)) {
                    specificBluetoothService = service;
                }
            }
            Thread.sleep(4000);
        } while (bluetoothServices.isEmpty());

        return specificBluetoothService;
    }
}
