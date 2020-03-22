package uibk.ac.at.prodiga.tests.helper;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class TestHelper {

    public static void autoWireTestClass(TestContext context,
                                         AbstractTestExecutionListener testClass) {
        context.getApplicationContext()
                .getAutowireCapableBeanFactory()
                .autowireBean(testClass);
    }
}
