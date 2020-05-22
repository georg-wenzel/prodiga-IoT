package uibk.ac.at.prodiga.model;

import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
public class Dice implements Persistable<Long>, Serializable {

    private static final long serialVersionUID = 1543543567314567565L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, length = 1337)
    private String internalId;

    @ManyToOne(optional = true, fetch = FetchType.EAGER, targetEntity = RaspberryPi.class)
    private RaspberryPi assignedRaspberry;

    @OneToOne(optional = true, fetch = FetchType.EAGER, targetEntity = User.class)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = User.class)
    private User objectCreatedUser;

    @Column(nullable = false)
    private Date objectCreatedDateTime;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    private User objectChangedUser;

    @Temporal(TemporalType.TIMESTAMP)
    private Date objectChangedDateTime;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = true)
    private Integer lastBatteryStatus;

    public Integer getLastBatteryStatus() {
        return lastBatteryStatus;
    }

    public void setLastBatteryStatus(Integer lastBatteryStatus) {
        this.lastBatteryStatus = lastBatteryStatus;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public RaspberryPi getAssignedRaspberry() {
        return assignedRaspberry;
    }

    public void setAssignedRaspberry(RaspberryPi assignedRaspberry) {
        this.assignedRaspberry = assignedRaspberry;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getObjectCreatedUser() {
        return objectCreatedUser;
    }

    public void setObjectCreatedUser(User objectCreatedUser) {
        this.objectCreatedUser = objectCreatedUser;
    }

    public Date getObjectCreatedDateTime() {
        return objectCreatedDateTime;
    }

    public void setObjectCreatedDateTime(Date objectCreatedDateTime) {
        this.objectCreatedDateTime = objectCreatedDateTime;
    }

    public User getObjectChangedUser() {
        return objectChangedUser;
    }

    public void setObjectChangedUser(User objectChangedUser) {
        this.objectChangedUser = objectChangedUser;
    }

    public Date getObjectChangedDateTime() {
        return objectChangedDateTime;
    }

    public void setObjectChangedDateTime(Date objectChangedDateTime) {
        this.objectChangedDateTime = objectChangedDateTime;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return this.objectCreatedDateTime == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dice dice = (Dice) o;
        return Objects.equals(id, dice.id) &&
                Objects.equals(internalId, dice.internalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, internalId);
    }
}
