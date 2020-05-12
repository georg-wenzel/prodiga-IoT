package uibk.ac.at.prodigaclient.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uibk.ac.at.prodigaclient.BluetoothUtility.Cube;

public class CubeTest {
    private Cube cube;

    @BeforeEach
    public void setUp() {
        cube = new Cube(BluetoothDeviceMockingHelper.mockBluetoothDevice());
    }

    @Test
    public void getCubeNameTest() {
        Assertions.assertEquals("TimeFlip", cube.getName());
    }

    @Test
    public void getCubeAddress() {
        Assertions.assertEquals("12:34:56:78:90:12", cube.getAddress());
    }

    @Test
    public void getCurrentSideTest() {
        Assertions.assertEquals(1, cube.getCurrentSide());
    }
}
