package uibk.ac.at.prodigaclient;

import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;

import java.util.List;

public class Cube {
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
}
