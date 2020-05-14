package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import org.mockito.*;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CubeManagerTest {

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
