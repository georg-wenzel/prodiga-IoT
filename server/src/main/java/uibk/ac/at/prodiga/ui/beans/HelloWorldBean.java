package uibk.ac.at.prodiga.ui.beans;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class HelloWorldBean {

    /**
     * Returns a hello-world-string.
     *
     * @return hello-world-string
     */
    public String getHello() {
        return "Hello from a JSF-managed bean!";
    }
}
