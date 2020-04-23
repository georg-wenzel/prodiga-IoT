package uibk.ac.at.prodigaclient;

import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;

public class ManageCubeMain {
    public static void main(String[] args) {
        CubeManager manageCubes = CubeManager.getInstance();
        manageCubes.updateDeviceList();
        System.out.println(manageCubes.getCubeIDList());
        System.out.println(manageCubes.getHistory("0C:61:CF:C7:8F:D5"));
        System.out.println(manageCubes.getBattery("0C:61:CF:C7:8F:D5") + "%");
        manageCubes.closeManager();
    }
}
