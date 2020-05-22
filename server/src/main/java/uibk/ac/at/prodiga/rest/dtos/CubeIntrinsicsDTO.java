package uibk.ac.at.prodiga.rest.dtos;

public class CubeIntrinsicsDTO {

    private String internalId;
    private Integer batteryStatus;

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public Integer getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(Integer batteryStatus) {
        this.batteryStatus = batteryStatus;
    }
}
