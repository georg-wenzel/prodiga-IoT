package uibk.ac.at.prodiga.ui.beans;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.services.MailService;
import uibk.ac.at.prodiga.utils.Constants;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

@Component
@Scope("session")
public class ExceptionHelperBean {

    private final ProdigaUserLoginManager prodigaUserLoginManager;
    private final MailService mailService;

    public ExceptionHelperBean(ProdigaUserLoginManager prodigaUserLoginManager, MailService mailService) {
        this.prodigaUserLoginManager = prodigaUserLoginManager;
        this.mailService = mailService;
    }

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

    /**
     * Gets the specific ui Severity
     * @param ex The exception to handle
     * @return The Severity used by the client
     */
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

    /**
     * Gets the String which will be displayed in the UI as the error
     * @param ex The exception to handle
     * @return The display String
     */
    public String getDisplaySeverity(Exception ex ){
        if(ex == null) {
            return "";
        }

        String result = "Error";

        if(ex instanceof ProdigaGeneralExpectedException) {
            ProdigaGeneralExpectedException pEx = (ProdigaGeneralExpectedException) ex;

            result = pEx.getSeverity();
        }

        return result;
    }

    /**
     * Returns the message which will bes displayed on the client
     * @param ex The exception to handle
     * @return The message to display
     */
    public String getDisplayMessage(Exception ex) {
        if(ex == null) {
            return "";
        }

        if(!StringUtils.isEmpty(ex.getMessage())) {
            return ex.getMessage();
        } else {
            return ex.getClass().getName();
        }
    }

    public void sendException(Exception ex) {
        StringBuilder builder = new StringBuilder();
        builder.append("Error Report!\n\n");

        User loggedInUser = prodigaUserLoginManager.getCurrentUser();

        builder.append("Reported by: ").append(loggedInUser == null ? "Unknown User" : loggedInUser.getUsername()).append("\n\n");

        if(ex != null){
            builder.append("Full Stacktrace:\n\n");
            builder.append(ex.toString());
        } else {
            builder.append("No exception Information!");
        }

        mailService.sendMail(Constants.DEFAULT_EMAIL_RECEIVER, "New Error Report!", builder.toString());
    }

}
