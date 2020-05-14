package uibk.ac.at.prodigaclient;

import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;

public class ManageCubeMain {
    public static void main(String[] args) throws InterruptedException {
        CubeManager manageCubes = CubeManager.getInstance();
        manageCubes.updateDeviceList();
        System.out.println(manageCubes.getCubeIDList());

        Thread.sleep(1000);

        System.out.println(manageCubes.getHistory("0C:61:CF:C7:8F:D5"));

        Thread.sleep(1000);

        System.out.println(manageCubes.getHistory("0C:61:CF:C7:8F:D5"));

        Thread.sleep(1000);

        System.out.println(manageCubes.getBattery("0C:61:CF:C7:8F:D5") + "%");

        Thread.sleep(1000);

        manageCubes.deleteHistory("0C:61:CF:C7:8F:D5");

        Thread.sleep(1000);

        System.out.println(manageCubes.getHistory("0C:61:CF:C7:8F:D5"));

        Thread.sleep(1000);

        manageCubes.connectToCube("0C:61:CF:C7:8F:D5");
        System.out.println(manageCubes.getCurrentSide("0C:61:CF:C7:8F:D5"));
        manageCubes.disconnectFromCube("0C:61:CF:C7:8F:D5");

        Thread.sleep(1000);

        manageCubes.closeManager();
    }
}
