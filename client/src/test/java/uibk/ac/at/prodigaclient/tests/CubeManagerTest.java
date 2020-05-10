package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.BeforeEach;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import org.mockito.*;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;


public class CubeManagerTest {
    @Mock
    private CubeManager cubeManagerMock;


    @BeforeEach
    public void setUp() {
        Set<String> set = new HashSet<String>();
        set.add("10:10:10:10:10:10:10");
        set.add("10:10:10:10:10:10:10");
        when(cubeManagerMock.getCubeIDList()).thenReturn(set);
    }
}
