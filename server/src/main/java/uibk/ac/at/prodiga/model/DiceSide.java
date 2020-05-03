package uibk.ac.at.prodiga.model;

import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class DiceSide implements Persistable<Long>, Serializable {

    private static final long serialVersionUID = 1321321657689783875L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int side;

    @Column(nullable = false)
    private int currentSeconds;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = Dice.class)
    Dice dice;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = BookingCategory.class)
    BookingCategory bookingCategory;

    @Column(nullable = false)
    private Date objectCreatedDateTime;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = User.class)
    User objectCreatedUser;

    @Column
    private Date objectChangedDateTime;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    User objectChangedUser;

    public int getCurrentSeconds() {
        return currentSeconds;
    }

    public void setCurrentSeconds(int currentSeconds) {
        this.currentSeconds = currentSeconds;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }

    public Dice getDice() {
        return dice;
    }

    public void setDice(Dice dice) {
        this.dice = dice;
    }

    public BookingCategory getBookingCategory() {
        return bookingCategory;
    }

    public void setBookingCategory(BookingCategory bookingCategory) {
        this.bookingCategory = bookingCategory;
    }

    public Date getObjectCreatedDateTime() {
        return objectCreatedDateTime;
    }

    public void setObjectCreatedDateTime(Date objectCreatedDateTime) {
        this.objectCreatedDateTime = objectCreatedDateTime;
    }

    public User getObjectCreatedUser() {
        return objectCreatedUser;
    }

    public void setObjectCreatedUser(User objectCreatedUser) {
        this.objectCreatedUser = objectCreatedUser;
    }

    public Date getObjectChangedDateTime() {
        return objectChangedDateTime;
    }

    public void setObjectChangedDateTime(Date objectChangedDateTime) {
        this.objectChangedDateTime = objectChangedDateTime;
    }

    public User getObjectChangedUser() {
        return objectChangedUser;
    }

    public void setObjectChangedUser(User objectChangedUser) {
        this.objectChangedUser = objectChangedUser;
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