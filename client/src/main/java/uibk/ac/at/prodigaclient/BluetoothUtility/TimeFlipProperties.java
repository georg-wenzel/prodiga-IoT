package uibk.ac.at.prodigaclient.BluetoothUtility;

public class TimeFlipProperties {
    public static final byte[] CUBEPASSWORD = {0x30, 0x30, 0x30, 0x30, 0x30, 0x30};
    public static final byte[] READHISTORYCMD = {0x01};
    public static final byte[] DELETEHISTORYCMD = {0x02};

    public static final String FACETSERVICEUUID = "f1196f50-71a4-11e6-bdf4-0800200c9a66";
    public static final String BATTERYSERVICEUUID = "0000180f-0000-1000-8000-00805f9b34fb";

    public static final String BATTERYCHARACTERISTICUUID = "00002a19-0000-1000-8000-00805f9b34fb";
    public static final String CURRENTFACETCHARACTERISTICUUID = "f1196f52-71a4-11e6-bdf4-0800200c9a66";
    public static final String COMMANDREADCHARACTERISTICUUID = "f1196f53-71a4-11e6-bdf4-0800200c9a66";
    public static final String COMMANDWRITERCHARACTERISTICUUID = "f1196f54-71a4-11e6-bdf4-0800200c9a66";
    public static final String PASSWORDCHARACTERISTICUUID = "f1196f57-71a4-11e6-bdf4-0800200c9a66";
}
