package uibk.ac.at.prodiga.utils;

public enum MessageType {
    ERROR("Error"),
    WARNING("Warning"),
    INFO("Info");

    private String label;

    MessageType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
