package uibk.ac.at.prodiga.model;

import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class LogInformation implements Persistable<Long>, Serializable {

    private static final long serialVersionUID = 4324236765214236432L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = User.class)
    private User objectedCreatedUser;

    @Temporal(TemporalType.TIMESTAMP)
    private Date objectedCreatedDateTime;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    private User objectedUpdatedUser;

    @Temporal(TemporalType.TIMESTAMP)
    private Date objectedUpdatedDateTime;

    @Column(nullable = false, length = 1337)
    private String text;

    public void setId(Long id) {
        Id = id;
    }

    public User getObjectedCreatedUser() {
        return objectedCreatedUser;
    }

    public void setObjectedCreatedUser(User objectedCreatedUser) {
        this.objectedCreatedUser = objectedCreatedUser;
    }

    public Date getObjectedCreatedDateTime() {
        return objectedCreatedDateTime;
    }

    public void setObjectedCreatedDateTime(Date objectedCreatedDateTime) {
        this.objectedCreatedDateTime = objectedCreatedDateTime;
    }

    public User getObjectedUpdatedUser() {
        return objectedUpdatedUser;
    }

    public void setObjectedUpdatedUser(User objectedUpdatedUser) {
        this.objectedUpdatedUser = objectedUpdatedUser;
    }

    public Date getObjectedUpdatedDateTime() {
        return objectedUpdatedDateTime;
    }

    public void setObjectedUpdatedDateTime(Date objectedUpdatedDateTime) {
        this.objectedUpdatedDateTime = objectedUpdatedDateTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Long getId() {
        return Id;
    }

    @Override
    public boolean isNew() {
        return false;
    }
}
