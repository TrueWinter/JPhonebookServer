package dev.truewinter.jphonebookserver;

import org.mindrot.jbcrypt.BCrypt;

public class Account {
    private int id;
    private String username;
    private String password;
    private AccountRoles role;
    private boolean active;

    public Account(int id, String username, String password, AccountRoles role, boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public AccountRoles getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public static boolean isCorrectPassword(String toCheck, Account account) {
        return isCorrectPassword(toCheck, account.getPassword());
    }

    public static boolean isCorrectPassword(String toCheck, String password) {
        return BCrypt.checkpw(toCheck, password);
    }
}
