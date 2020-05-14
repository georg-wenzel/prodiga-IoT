package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import tinyb.BluetoothManager;
import uibk.ac.at.prodigaclient.BluetoothUtility.Cube;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.tests.MockCreators.BluetoothDeviceMockCreator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.LinkedList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CubeManagerTest {
    @Mock
    BluetoothManager mockmanager;

    CubeManager cubeManager;


    @BeforeEach
    public void setUp() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        MockitoAnnotations.initMocks(this);
        Constructor<CubeManager> c = CubeManager.class.getDeclaredConstructor(BluetoothManager.class); // whoa didn't know this works
        c.setAccessible(true);

        when(mockmanager.startDiscovery()).thenReturn(true);

        cubeManager = c.newInstance(mockmanager);
    }

    @Test
    public void constructorTest() {
        Assertions.assertNotNull(cubeManager);
    }
}
