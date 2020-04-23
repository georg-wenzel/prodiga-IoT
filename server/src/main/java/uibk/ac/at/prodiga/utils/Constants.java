package uibk.ac.at.prodiga.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Constants {

    public static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    public static final String DEFAULT_EMAIL_RECEIVER = "prodiga.project@gmail.com";
    public static final Long DO_NOT_BOOK_BOOKING_CATEGORY_ID = 12L;
}
