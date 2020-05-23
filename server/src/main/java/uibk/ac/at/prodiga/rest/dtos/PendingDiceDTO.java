package uibk.ac.at.prodiga.rest.dtos;

public class PendingDiceDTO {

    private String diceInternalId;
    private String raspiInternalId;

    public String getDiceInternalId() {
        return diceInternalId;
    }

    public void setDiceInternalId(String diceInternalId) {
        this.diceInternalId = diceInternalId;
    }

    public String getRaspiInternalId() {
        return raspiInternalId;
    }

    public void setRaspiInternalId(String raspiInternalId) {
        this.raspiInternalId = raspiInternalId;
    }
}
