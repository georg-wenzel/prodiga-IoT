package uibk.ac.at.prodiga.model;

import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class Team implements Persistable<Long>, Serializable {

    private static final long serialVersionUID = 1543543567124567544L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 300)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = Department.class)
    private Department department;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = User.class)
    private User objectCreatedUser;

    @Column(nullable = false)
    private Date objectCreatedDateTime;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    private User objectChangedUser;

    @Temporal(TemporalType.TIMESTAMP)
    private Date objectChangedDateTime;

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }


    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return objectCreatedDateTime == null;
    }
}
