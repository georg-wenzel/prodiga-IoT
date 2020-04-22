// From https://mkyong.com/java/how-to-get-mac-address-in-java/

package uibk.ac.at.prodigaclient;

import java.net.InetAddress;
import java.net.NetworkInterface;

public class Constants {

    private static final Object jwtLock = new Object();
    private static volatile  String jwt = null;

    private static String macAddress = null;
    private static String password = null;

    public static final int DEFAULT_WAIT_TIMEOUT_MILLIS = 20000;

    private static ApiClient client = null;

    public static String getInternalId() {
        if(macAddress == null) {
            try {
                InetAddress ip = InetAddress.getLocalHost();

                NetworkInterface network = NetworkInterface.getByInetAddress(ip);

                byte[] mac = network.getHardwareAddress();

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }

                macAddress = sb.toString();

            } catch (Exception ex) {
                throw new RuntimeException("Error while reading MAC-Address!", ex);
            }
        }

        return macAddress;
    }

    public static ApiClient getClient() {
        if(client == null) {
            client = new ApiClient("JWT");
        }
        return client;
    }

    public static String getJwt() {
        synchronized (jwtLock) {
            return jwt;
        }
    }

    public static void setJwt(String newJwt) {
        synchronized (jwtLock) {
            Constants.jwt = newJwt;
            client.setApiKey("Bearer " + jwt);
        }
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        Constants.password = password;
    }
}
