package uibk.ac.at.prodiga.model;

import org.springframework.data.domain.Persistable;
import uibk.ac.at.prodiga.utils.badge.Badge;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity for saving the Batches in the DB.
 *
 */
@Entity
public class BadgeDB implements Persistable<Long>, Serializable {

    private static final long serialVersionUID = 1543543567124567565L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String badgeName;

    @ManyToOne(optional = false, targetEntity = User.class, fetch = FetchType.EAGER)
    private User user;

    @Column(nullable = false)
    private Date date;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return user == null;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
