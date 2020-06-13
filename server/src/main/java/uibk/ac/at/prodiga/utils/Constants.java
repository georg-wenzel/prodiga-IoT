package uibk.ac.at.prodiga.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.InetAddress;

public class Constants {

    public static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    public static final String DEFAULT_EMAIL_RECEIVER = "prodiga.project@gmail.com";
    public static final Long DO_NOT_BOOK_BOOKING_CATEGORY_ID = 1L;
    public static final Long VACATION_BOOKING_ID = 13L;

    private static String ipAddress = null;

    public static String getIpAddress() throws ProdigaGeneralExpectedException {
        if(ipAddress == null) {
            try {
                ipAddress = "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/";
            } catch (Exception ex) {
                throw new ProdigaGeneralExpectedException("Error while reading IP Address", MessageType.ERROR);
            }
        }
        return ipAddress;
    }
}
