package uibk.ac.at.prodiga.model;

public enum FrequencyType {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    private String label;

    FrequencyType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}
