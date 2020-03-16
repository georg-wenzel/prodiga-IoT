package uibk.ac.at.prodiga.model;

import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class RaspberryPi implements Persistable<Long>, Serializable {

    private static final long serialVersionUID = 1543543567124562365L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 1337)
    private String internalId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = User.class)
    private User objectCreatedUser;

    @Column(nullable = false)
    private Date objectCreatedDateTime;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = User.class)
    private User objectChangedUser;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date objectChangedDateTime;

    public void setId(Long id) {
        this.id = id;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
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
}
