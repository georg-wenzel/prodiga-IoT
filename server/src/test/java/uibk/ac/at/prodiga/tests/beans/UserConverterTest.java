package uibk.ac.at.prodiga.tests.beans;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.beans.UserConverter;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class UserConverterTest {

    @Autowired
    UserConverter userConverter;

    @Autowired
    UserRepository userRepository;

    @Test
    @DirtiesContext
    public void userConverter_getAndSetUser_correctlyReturned() {
        User u = DataHelper.createAdminUser("admin", userRepository);

        Assertions.assertNull(userConverter.getAsString(null, null, ""));
        Assertions.assertEquals("admin", userConverter.getAsString(null, null, u));

        Assertions.assertEquals(u, userConverter.getAsObject(null, null, "admin"));
    }
}
