package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tinyb.BluetoothManager;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.tests.MockCreators.BluetoothManagerMockCreator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

public class CubeManagerTest {
    BluetoothManager mockManager;

    CubeManager cubeManager;

    @BeforeEach
    public void setUp() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<CubeManager> c = CubeManager.class.getDeclaredConstructor(BluetoothManager.class); // whoa didn't know this works
        c.setAccessible(true);

        mockManager = BluetoothManagerMockCreator.mockBluetoothManager();

        cubeManager = c.newInstance(mockManager);
    }

    @Test
    public void constructorTest() {
        Assertions.assertNotNull(cubeManager);
    }


    @Test
    public void testGetCubeIDList() {
        cubeManager.updateDeviceList();
        Set<String> availableCubes = cubeManager.getCubeIDList();

        Assertions.assertNotNull(availableCubes);
        Assertions.assertEquals(2, availableCubes.size());
    }
}
