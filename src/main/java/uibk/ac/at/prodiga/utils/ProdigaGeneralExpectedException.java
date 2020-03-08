package uibk.ac.at.prodiga.utils;

import java.util.concurrent.Future;

public class ProdigaGeneralExpectedException extends Exception {

    private MessageType type;
    private String severity;

    public ProdigaGeneralExpectedException(String message, MessageType type, Throwable inner) {
        super(message, inner);
        setType(type);
    }

    /**
     * Excepted Exception - used to display on the client
     * @param message The message
     * @param type The type
     */
    public ProdigaGeneralExpectedException(String message, MessageType type) {
        super(message);
        setType(type);
    }

    public MessageType getType() {
        return type;
    }

    private void setType(MessageType type) {
        this.type = type;

        switch (type) {
            case INFO:
                this.severity = "info";
            case WARNING:
                this.severity = "warn";
            case ERROR:
                this.severity = "error";
        }
    }

    /**
     * Checks if the given exception wrappes a {@link GeneralExpectedException}
     * @param ex The exception to check
     * @throws Exception Either a {@link GeneralExpectedException} if wrapped otherwise {@param ex}
     */
    public static void throwWrappedException(Exception ex) throws Exception {
        if(ex instanceof ProdigaGeneralExpectedException) {
            throw ex;
        }

        if(ex.getCause() instanceof ProdigaGeneralExpectedException) {
            throw (ProdigaGeneralExpectedException) ex.getCause();
        }

        throw ex;
    }
}
