package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.tests.MockCreators.CubeManagerMockCreator;

public class CubeManagerMockTest {

    private CubeManager cubeManagerMock;

    @BeforeEach
    public void setUp() {
        cubeManagerMock = CubeManagerMockCreator.mockFullCubeManager();
    }

    @Test
    void testCubeManagerMock() {
        Assertions.assertEquals(1, cubeManagerMock.getCurrentSide("0C:61:CF:C7:8F:D5"));
    }
}
