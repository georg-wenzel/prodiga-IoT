package uibk.ac.at.prodiga.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.*;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Persistable;

@Entity
public class User implements Persistable<String>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 100, nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    private User createUser;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    private User updateUser;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    private String password;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    @Column(columnDefinition =  "boolean default false")
    private boolean mayEditHistoricData;

    boolean enabled;

    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "User_UserRole")
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles;

    @ManyToOne(targetEntity = Team.class, fetch = FetchType.EAGER, optional = true)
    private Team assignedTeam;

    @ManyToOne(targetEntity = Department.class, fetch = FetchType.EAGER, optional = true)
    private Department assignedDepartment;

    @Column(nullable = true)
    private FrequencyType frequencyType;

    @Column(columnDefinition = "boolean default false")
    private Boolean notificationsEnabled;

    public FrequencyType getFrequencyType() {
        if (frequencyType == null){
            frequencyType = FrequencyType.WEEKLY;
        }
        return frequencyType;
    }

    public void setFrequencyType(FrequencyType frequencyType) {
        this.frequencyType = frequencyType;
    }

    public Boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(Boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public Team getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(Team assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    public Department getAssignedDepartment() {
        return assignedDepartment;
    }

    public void setAssignedDepartment(Department assignedDepartment) {
        this.assignedDepartment = assignedDepartment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public User getCreateUser() {
        return createUser;
    }

    public void setCreateUser(User createUser) {
        this.createUser = createUser;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public User getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(User updateUser) {
        this.updateUser = updateUser;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public boolean getMayEditHistoricData() {
        return mayEditHistoricData;
    }

    public void setMayEditHistoricData(boolean mayEditHistoricData) {
        this.mayEditHistoricData = mayEditHistoricData;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.username);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof User)) {
            return false;
        }
        final User other = (User) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uibk.ac.at.prodiga.model.User[ id=" + username + " ]";
    }

    @Override
    public String getId() {
        return getUsername();
    }

    public void setId(String id) {
        setUsername(id);
    }

    @Override
    public boolean isNew() {
        return (null == createDate);
    }

    /**
     * Here is where Java gets weird. The view doesn't know how to handle enums - so we have to
     * provide a setter and getter with strings Here we return all {@link UserRole#label} of the users
     * roles
     *
     * @return A set of all {@link UserRole} but only the {@link UserRole#label}
     */
    public Set<String> getRolesAsString() {
        if (roles == null) {
            roles = new HashSet<>();
        }
        return this.roles.stream().map(UserRole::getLabel).collect(Collectors.toSet());
    }

    /**
     * Same as the getter. The given roles will be "casted" to {@link UserRole} enum members
     * @param roles Set of {@link UserRole} but only the {@link UserRole#label}
     */
    public void setRolesAsString(Set<String> roles) {
        Set<UserRole> userRoles = new HashSet<>();
        for(String string : roles){
            if(string.equals("Department leader")){
                userRoles.add(UserRole.valueOf(UserRole.class, "DEPARTMENTLEADER"));
            }
            else if(string.equals("Team leader")){
                userRoles.add(UserRole.valueOf(UserRole.class,"TEAMLEADER"));
            }
            else{
                userRoles.add(UserRole.valueOf(UserRole.class, string.toUpperCase()));
            }

        }
        this.roles = userRoles;
    }

}
