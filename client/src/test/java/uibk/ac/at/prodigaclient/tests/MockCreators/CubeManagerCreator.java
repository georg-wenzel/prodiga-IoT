package uibk.ac.at.prodigaclient.tests.MockCreators;

import tinyb.BluetoothManager;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class CubeManagerCreator {
    public static CubeManager createCustomCubeManagerInstance(BluetoothManager bluetoothManager) {
        CubeManager cubeManager = null;
        Constructor<CubeManager> c = null;

        try { // Didn't know this works
            c = CubeManager.class.getDeclaredConstructor(BluetoothManager.class);
            c.setAccessible(true);
            cubeManager = c.newInstance(bluetoothManager);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return  cubeManager;
    }
}
