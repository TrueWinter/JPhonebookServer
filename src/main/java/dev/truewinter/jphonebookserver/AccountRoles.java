package dev.truewinter.jphonebookserver;

public enum AccountRoles {
    USER("user"),
    ADMIN("admin");

    private String role;

    AccountRoles(String role) {
        this.role = role;
    }

    public boolean equalsRole(String role) {
        return this.role.equals(role);
    }

    public String toString() {
        return this.role;
    }

    public static AccountRoles fromString(String role) {
        for (AccountRoles r : AccountRoles.values()) {
            if (r.role.equalsIgnoreCase(role)) {
                return r;
            }
        }

        return null;
    }
}
