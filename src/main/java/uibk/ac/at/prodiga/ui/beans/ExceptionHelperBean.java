package uibk.ac.at.prodiga.ui.beans;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

@Component
@Scope("request")
public class ExceptionHelperBean {

    /**
     * Determines whether to show the exception in the UI
     * @param ex The exception
     * @return True if the exception should be shown
     */
    public boolean displayException(Exception ex) {
        if(ex == null) {
            return false;
        }

        if(ex instanceof ProdigaGeneralExpectedException) {
            ProdigaGeneralExpectedException pEx = (ProdigaGeneralExpectedException) ex;

            switch (pEx.getType()) {
                case WARNING:
                case INFO:
                    return false;
            }
        }

        return true;
    }

    public String getUiSeverity(Exception ex) {
        String result = "error";

        if(ex instanceof ProdigaGeneralExpectedException) {
            ProdigaGeneralExpectedException pEx = (ProdigaGeneralExpectedException) ex;

            switch (pEx.getType()) {
                case INFO:
                    result = "info";
                    break;
                case WARNING:
                    result = "warn";
                    break;
            }
        }

        return result;
    }

}
