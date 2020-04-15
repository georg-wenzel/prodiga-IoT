package uibk.ac.at.prodigaclient;

import tinyb.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

public class BluetoothTest {
    static boolean running = true;

    static void printDevice(BluetoothDevice device) {
        System.out.print("Address = " + device.getAddress());
        System.out.print(" Name = " + device.getName());
        System.out.print(" Connected = " + device.getConnected());
        System.out.println();
    }

    static BluetoothDevice getDevice() throws InterruptedException {
        BluetoothManager manager = BluetoothManager.getBluetoothManager();
        BluetoothDevice cube = null;
        for (int i = 0; (i < 15) && running; ++i) {
            List<BluetoothDevice> list = manager.getDevices();
            if (list == null)
                return null;

            for (BluetoothDevice device : list) {
                printDevice(device);
                if (device.getName().toLowerCase().contains("timeflip"))
                    cube = device;
            }

            if (cube != null) {
                return cube;
            }
            Thread.sleep(4000);
        }
        return null;
    }

    static BluetoothGattService getService(BluetoothDevice device, String UUID) throws InterruptedException {
        System.out.println("Services exposed by device:");
        BluetoothGattService specificBluetoothService = null;
        List<BluetoothGattService> bluetoothServices = null;

        do {
            bluetoothServices = device.getServices();
            if (bluetoothServices == null)
                return null;

            for (BluetoothGattService service : bluetoothServices) {
                System.out.println("UUID: " + service.getUUID());
                if (service.getUUID().equals(UUID))
                    specificBluetoothService = service;
            }
            Thread.sleep(4000);
        } while (bluetoothServices.isEmpty() && running);

        return specificBluetoothService;
    }

    static BluetoothGattCharacteristic getCharacteristic(BluetoothGattService service, String UUID) {
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        if (characteristics == null)
            return null;

        for (BluetoothGattCharacteristic characteristic : characteristics) {
            if (characteristic.getUUID().equals(UUID))
                return characteristic;
        }
        return null;
    }

    static boolean is_last(byte [] test) {
        for (byte x : test) {
            if (x != 0x00) {
                return false;
            }
        }
        return true;
    }

    // facets decoding
    static int getFacet(byte [] byteArray) {
        return byteArray[2] >> 2;
    }

    // little endian conversion
    static int getTime(byte [] byteArray) {
        return ((Byte.toUnsignedInt(byteArray[2]) & 0x03) << 16) | (Byte.toUnsignedInt(byteArray[1]) << 8)
                | (Byte.toUnsignedInt(byteArray[0]));
    }

    public static void main(String[] args) throws InterruptedException {

        /*
         * To start looking of the device, we first must initialize the TinyB library. The way of interacting with the
         * library is through the BluetoothManager. There can be only one BluetoothManager at one time, and the
         * reference to it is obtained through the getBluetoothManager method.
         */
        BluetoothManager manager = BluetoothManager.getBluetoothManager();

        /*
         * The manager will try to initialize a BluetoothAdapter if any adapter is present in the system. To initialize
         * discovery we can call startDiscovery, which will put the default adapter in discovery mode.
         */
        boolean discoveryStarted = manager.startDiscovery();

        System.out.println("The discovery started: " + discoveryStarted);
        BluetoothDevice cube = getDevice();

        /*
         * After we find the device we can stop looking for other devices.
         */
        try {
            manager.stopDiscovery();
        } catch (BluetoothException e) {
            System.err.println("Discovery could not be stopped.");
        }

        if (cube == null) {
            System.err.println("No cube found with the provided address.");
            System.exit(-1);
        }

        System.out.print("Found device: ");
        printDevice(cube);

        if (cube.connect())
            System.out.println("Cube with the provided address connected");
        else {
            System.out.println("Could not connect device.");
            System.exit(-1);
        }

        Lock lock = new ReentrantLock();
        Condition cv = lock.newCondition();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            lock.lock();
            try {
                cv.signalAll();
            } finally {
                lock.unlock();
            }
        }));


        BluetoothGattService facetService = getService(cube, "f1196f50-71a4-11e6-bdf4-0800200c9a66"); // TimeFlip Service
        BluetoothGattService batteryService = getService(cube, "0000180f-0000-1000-8000-00805f9b34fb"); // TimeFlip get battery

        if (facetService == null) {
            System.err.println("This device does not have the facet service we are looking for. Mabe its not a time flip cube");
            cube.disconnect();
            System.exit(-1);
        }

        System.out.println("Found service " + facetService.getUUID());

        BluetoothGattCharacteristic batteryChar = getCharacteristic(batteryService, "00002a19-0000-1000-8000-00805f9b34fb"); // get battery
        BluetoothGattCharacteristic passwordChar = getCharacteristic(facetService, "f1196f57-71a4-11e6-bdf4-0800200c9a66"); // Password {0x30, 0x30, 0x30, 0x30, 0x30, 0x30}
        BluetoothGattCharacteristic facetChar = getCharacteristic(facetService, "f1196f52-71a4-11e6-bdf4-0800200c9a66"); // Facet
        BluetoothGattCharacteristic commandOutputChar = getCharacteristic(facetService, "f1196f53-71a4-11e6-bdf4-0800200c9a66"); // command output characteristic used to read the history
        BluetoothGattCharacteristic commandInputChar = getCharacteristic(facetService, "f1196f54-71a4-11e6-bdf4-0800200c9a66"); // command input characteristic

        if (passwordChar == null) {
            System.err.println("Could not find password service. Something went wrong");
            cube.disconnect();
            System.exit(-1);
        }

        System.out.println("Start the service. Insert Passwort");

        /*
         * Activate the services by inserting the password
         */
        byte[] config = {0x30, 0x30, 0x30, 0x30, 0x30, 0x30};
        passwordChar.writeValue(config);

        if (batteryChar != null) {
            byte[] batteryValue = batteryChar.readValue();
            System.out.println("Battery = { " + String.format("%02x", batteryValue[0]) + " }");
            System.out.println("Battery = " + (batteryValue[0] & 0xff) + "%");
        } else {
            System.out.println("Something went wrong with the battery characteristics");
        }

        /*
         * Read history
         */
        byte[] command = {0x01}; // command 0x01 = read out history
        commandInputChar.writeValue(command);

        if (commandOutputChar != null) {
            byte[] history = commandOutputChar.readValue();

            while (!is_last(history)) {
                System.out.print("History = {");

                for (byte b : history) {
                    System.out.print(String.format("%02x,", b));
                }

                System.out.println("}");

                System.out.println("Facet: " + getFacet(Arrays.copyOfRange(history, 0, 3)) + " Time: " +
                        getTime(Arrays.copyOfRange(history, 0, 3)));
                System.out.println("Facet: " + getFacet(Arrays.copyOfRange(history, 3, 6)) + " Time: " +
                        getTime(Arrays.copyOfRange(history, 3, 6)));
                System.out.println("Facet: " + getFacet(Arrays.copyOfRange(history, 6, 9)) + " Time: " +
                        getTime(Arrays.copyOfRange(history, 6, 9)));
                System.out.println("Facet: " + getFacet(Arrays.copyOfRange(history, 9, 12)) + " Time: " +
                        getTime(Arrays.copyOfRange(history, 9, 12)));
                System.out.println("Facet: " + getFacet(Arrays.copyOfRange(history, 12, 15)) + " Time: " +
                        getTime(Arrays.copyOfRange(history, 12, 15)));
                System.out.println("Facet: " + getFacet(Arrays.copyOfRange(history, 15, 18)) + " Time: " +
                        getTime(Arrays.copyOfRange(history, 15, 18)));
                System.out.println("Facet: " + getFacet(Arrays.copyOfRange(history, 18, 21)) + " Time: " +
                        getTime(Arrays.copyOfRange(history, 18, 21)));

                history = commandOutputChar.readValue();
            }
        } else {
            System.out.println("Something went wrong with the battery characteristics");
        }

        /*
         * Each second read the value characteristic and display it in a human readable format.
         */
        while (running) {

            byte[] facet = facetChar.readValue();
            System.out.println("Facet ID = { " + String.format("%02x", facet[0]) + " }");

            /*
             * print out facets
             */
            System.out.println("Facet ID (in int) = { " + (facet[0] & 0xff) + " }");

            running = false;
            lock.lock();
            try {
                cv.await(1, TimeUnit.SECONDS);
            } finally {
                lock.unlock();
            }
        }

        cube.disconnect();
    }
}

