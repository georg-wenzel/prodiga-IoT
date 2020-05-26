package uibk.ac.at.prodiga.tests.beans;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.ui.beans.ExceptionHelperBean;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class ExceptionHelperBeanTest {

    @Autowired
    ExceptionHelperBean exceptionHelperBean;

    @Test
    public void exceptionHelperBean_getUiSeverity_returnsCorrectSeverity() {
        Assertions.assertEquals("error", exceptionHelperBean.getUiSeverity(new NullPointerException()));
        Assertions.assertEquals("info", exceptionHelperBean.getUiSeverity(new ProdigaGeneralExpectedException("", MessageType.INFO)));
        Assertions.assertEquals("warn", exceptionHelperBean.getUiSeverity(new ProdigaGeneralExpectedException("", MessageType.WARNING)));
        Assertions.assertEquals("error", exceptionHelperBean.getUiSeverity(new ProdigaGeneralExpectedException("", MessageType.ERROR)));
    }

    @Test
    public void exceptionHelperBean_getDisplaySeverity_returnsCorrectSeverity() {
        Assertions.assertEquals("", exceptionHelperBean.getDisplaySeverity(null));
        Assertions.assertEquals("Error", exceptionHelperBean.getDisplaySeverity(new NullPointerException()));
        Assertions.assertEquals("Info", exceptionHelperBean.getDisplaySeverity(new ProdigaGeneralExpectedException("", MessageType.INFO)));
        Assertions.assertEquals("Warning", exceptionHelperBean.getDisplaySeverity(new ProdigaGeneralExpectedException("", MessageType.WARNING)));
        Assertions.assertEquals("Error", exceptionHelperBean.getDisplaySeverity(new ProdigaGeneralExpectedException("", MessageType.ERROR)));
    }

    @Test
    public void exceptionHelperBean_getDisplayMessage_returnsCorrectMessage() {
        Assertions.assertEquals("", exceptionHelperBean.getDisplayMessage(null));
        Assertions.assertEquals(NullPointerException.class.getName(), exceptionHelperBean.getDisplayMessage(new NullPointerException()));
        Assertions.assertEquals("Test", exceptionHelperBean.getDisplayMessage(new ProdigaGeneralExpectedException("Test", MessageType.INFO)));
    }
}
