package uibk.ac.at.prodigaclient.dtos;

import java.util.UUID;

public class FeedDTO {

    private String internalId;
    private FeedAction feedAction;
    private DeviceType deviceType;
    private UUID id;

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public FeedAction getFeedAction() {
        return feedAction;
    }

    public void setFeedAction(FeedAction feedAction) {
        this.feedAction = feedAction;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
