package uibk.ac.at.prodiga.model;

public enum UserRole {
    ADMIN("Admin"),
    TEAMLEADER("Team leader"),
    DEPARTMENTLEADER("Department leader"),
    EMPLOYEE("Employee");

    private String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

}
