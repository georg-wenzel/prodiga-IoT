package uibk.ac.at.prodiga.utils;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

public class SnackbarHelper {

    private static final Object lock = new Object();
    private static SnackbarHelper instance = null;

    private SnackbarHelper() {
    }

    public static synchronized SnackbarHelper getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new SnackbarHelper();
                }
            }
        }

        return instance;
    }

    /**
     * Show snack bar.
     *
     * @param message the message
     * @param type the type
     */
    public void showSnackBar(String message, MessageType type) {
        if (message == null) {
            return;
        }

        Severity severity = null;

        switch (type) {
            case INFO:
                severity = FacesMessage.SEVERITY_INFO;
            case WARNING:
                severity = FacesMessage.SEVERITY_WARN;
            case ERROR:
                severity = FacesMessage.SEVERITY_ERROR;
            default:
                severity = FacesMessage.SEVERITY_INFO;
        }

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, message, ""));
    }
}
