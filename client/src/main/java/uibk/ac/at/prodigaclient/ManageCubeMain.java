package uibk.ac.at.prodigaclient;

public class ManageCubeMain {
    public static void main(String[] args) {
        ManageCubes manageCubes = new ManageCubes();
        manageCubes.updateDeviceList();
        System.out.println(manageCubes.getCubeIDList());
        System.out.println(manageCubes.getHistory("0C:61:CF:C7:8F:D5"));
        System.out.println(manageCubes.getBattery("0C:61:CF:C7:8F:D5") + "%");
        manageCubes.closeManager();
    }
}
