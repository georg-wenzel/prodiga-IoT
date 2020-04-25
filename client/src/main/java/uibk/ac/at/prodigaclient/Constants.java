// From https://mkyong.com/java/how-to-get-mac-address-in-java/

package uibk.ac.at.prodigaclient;

import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.api.AuthControllerApi;
import uibk.ac.at.prodigaclient.api.CubeControllerApi;
import uibk.ac.at.prodigaclient.api.IntrinsicsControllerApi;
import uibk.ac.at.prodigaclient.utils.Action;

import java.net.InetAddress;
import java.net.NetworkInterface;

public class Constants {

    private static final Object jwtLock = new Object();
    private static volatile  String jwt = null;
    private static Action authAction;

    private static String macAddress = null;
    private static String password = null;
    private static AuthControllerApi authControllerApi = null;
    private static IntrinsicsControllerApi intrinsicsControllerApi = null;
    private static CubeControllerApi cubeControllerApi = null;
    private static CubeManager cubeManager = null;

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
        synchronized (jwtLock) {
            return client;
        }
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

    public static Action getAuthAction() {
        return authAction;
    }

    public static void setAuthAction(Action authAction) {
        Constants.authAction = authAction;
    }

    public static AuthControllerApi getAuthControllerApi() {
        if(authControllerApi == null) {
            authControllerApi = getClient().createService(AuthControllerApi.class);
        }
        return authControllerApi;
    }

    public static CubeControllerApi getCubeControllerApi() {
        if(cubeControllerApi == null) {
            cubeControllerApi = getClient().createService(CubeControllerApi.class);
        }
        return cubeControllerApi;
    }

    public static IntrinsicsControllerApi getIntrinsicsControllerApi() {
        if(intrinsicsControllerApi == null) {
            intrinsicsControllerApi = getClient().createService(IntrinsicsControllerApi.class);
        }
        return intrinsicsControllerApi;
    }

    public static CubeManager getCubeManager() {
        if(cubeManager == null) {
            cubeManager = new CubeManager();
        }
        return cubeManager;
    }
}
