package uibk.ac.at.prodiga.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Constants {

    public static PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

}
