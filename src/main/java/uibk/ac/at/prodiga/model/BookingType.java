package uibk.ac.at.prodiga.model;

import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Booking Type (Category) within the system.
 * Boolean active determines whether the type is outdated or is still used for new bookings.
 * int side determines which side of the dice this category is used for.
 * String activityName is the name of the activity (e.g. Development, Design...)
 */
@Entity
public class BookingType implements Persistable<Long>, Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 64)
    private String activityName;

    @Column
    private boolean isActive;

    @Column(nullable = false)
    private int side;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = User.class)
    private User objectCreatedUser;

    @Column(nullable = false)
    private Date objectCreatedDateTime;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    private User objectChangedUser;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date objectChangedDateTime;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public boolean isNew()
    {
        return this.objectCreatedDateTime == null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActivityName()
    {
        return activityName;
    }

    public void setActivityName(String activityName) {
        if(activityName.length() < 2 || activityName.length() > 64)
            throw new RuntimeException("Acitivy Name must be between 2 and 64 characters.");
        this.activityName = activityName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side)
    {
        if(side < 1 || side > 12)
            throw new RuntimeException("Side must be between 1 and 12.");
        this.side = side;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingType that = (BookingType) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
