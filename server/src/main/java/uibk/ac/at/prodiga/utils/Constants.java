package uibk.ac.at.prodiga.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Constants {

    public static PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    public static String DEFAULT_EMAIL_RECEIVER = "prodiga.project@gmail.com";
    public static Long DO_NOT_BOOK_BOOKING_CATEGORY_ID = 12L;
}
