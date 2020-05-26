package uibk.ac.at.prodiga.utils;

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
                this.severity = "Info";
                break;
            case WARNING:
                this.severity = "Warning";
                break;
            case ERROR:
                this.severity = "Error";
                break;
        }
    }

    public String getSeverity() {
        return severity;
    }
}
