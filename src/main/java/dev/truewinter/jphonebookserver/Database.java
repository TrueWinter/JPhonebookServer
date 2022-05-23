package dev.truewinter.jphonebookserver;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Database {
    private static Database instance = null;

    private Database() throws Exception {
        initTables();

    }

    private Connection createConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        Path dbPath = Path.of(Util.getInstallPath().toString(), "data.db");
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    // There should only be one instance of the database, hence the singleton
    protected static Database getInstance() throws Exception {
        if (instance == null) {
            instance = new Database();
        }

        return instance;
    }

    private void initTables() throws Exception{
        String createDirectoriesTableSQL = "CREATE TABLE IF NOT EXISTS directories("
                + "id integer PRIMARY KEY, "
                + "name varchar(255)"
                + ");";

        String createContactsTableSQL = "CREATE TABLE IF NOT EXISTS contacts("
                + "id integer PRIMARY KEY, "
                + "directory_id integer, "
                + "name varchar(255), "
                + "telephone varchar(20), "
                + "mobile varchar(20), "
                + "other varchar(20), "
                // TODO: find out what ring does and implement it
                + "ring integer, "
                // this threw an error when it was "group"
                + "groupName varchar(30), "
                + "FOREIGN KEY (directory_id) REFERENCES directories(id)"
                + ");";

        String createLoginTableSQL = "CREATE TABLE IF NOT EXISTS accounts("
                + "id integer PRIMARY KEY, "
                + "username varchar(255), "
                + "password varchar(60), "
                + "role varchar(20), "
                + "active boolean"
                + ");";

        runSQL(createDirectoriesTableSQL);
        runSQL(createContactsTableSQL);
        runSQL(createLoginTableSQL);

        if (getAccountByUsername("admin").isEmpty()) {
            this.addAccount("admin", "phonebook", AccountRoles.ADMIN);
            System.out.println("Created default admin account with username admin and password phonebook");
        }
    }

    private void runSQL(String sql) throws Exception {
        Connection con = createConnection();
        con.createStatement().execute(sql);
        con.close();
    }

    public void addAccount(String username, String password, AccountRoles role) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("INSERT INTO accounts(username, password, role, active) VALUES(?, ?, ?, ?)");
        stmt.setString(1, username);
        stmt.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
        stmt.setString(3, role.toString());
        stmt.setBoolean(4, true);

        stmt.execute();
        con.close();
    }

    public Optional<Account> getAccountByID(int id) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("SELECT * FROM accounts WHERE id=?");
        stmt.setInt(1, id);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int id1 = rs.getInt("id");
            String username = rs.getString("username");
            String password = rs.getString("password");
            AccountRoles role = AccountRoles.fromString(rs.getString("role"));
            boolean active = rs.getBoolean("active");

            con.close();

            if (Util.hasNull(username, password, role, active)) {
                System.err.println("Failed to load account \"" + id1 + "\" from database, has null values");
                return Optional.empty();
            }

            return Optional.of(new Account(id1, username, password, role, active));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Account> getAccountByUsername(String username) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("SELECT * FROM accounts WHERE username=?");
        stmt.setString(1, username);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("id");
            String username1 = rs.getString("username");
            String password = rs.getString("password");
            AccountRoles role = AccountRoles.fromString(rs.getString("role"));
            boolean active = rs.getBoolean("active");

            con.close();

            if (Util.hasNull(username1, password, role, active)) {
                System.err.println("Failed to load account \"" + id + "\" from database");
                return Optional.empty();
            }

            return Optional.of(new Account(id, username1, password, role, active));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Account> getAccountByUsernameIfPasswordIsCorrect(String username, String password) throws Exception {
        return getAccountByUsername(username).filter(a -> Account.isCorrectPassword(password, a));
    }

    public List<Account> getAllAccounts() throws Exception {
        List<Account> accounts = new ArrayList<>();

        Connection con = createConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM accounts");

        while (rs.next()) {
            int id = rs.getInt("id");
            String username1 = rs.getString("username");
            String password = rs.getString("password");
            AccountRoles role = AccountRoles.fromString(rs.getString("role"));
            boolean active = rs.getBoolean("active");

            if (Util.hasNull(username1, password, role, active)) {
                continue;
            }

            accounts.add(new Account(id, username1, password, role, active));
        }

        con.close();

        return accounts;
    }

    public void changeUsername(int id, String newUsername) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("UPDATE accounts SET username=? WHERE id=?");
        stmt.setString(1, newUsername);
        stmt.setInt(2, id);

        stmt.execute();
        con.close();
    }

    public void changePassword(int id, String newPassword) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("UPDATE accounts SET password=? WHERE id=?");
        stmt.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        stmt.setInt(2, id);

        stmt.execute();
        con.close();
    }

    public void changeRole(int id, AccountRoles newRole) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("UPDATE accounts SET role=? WHERE id=?");
        stmt.setString(1, newRole.toString());
        stmt.setInt(2, id);

        stmt.execute();
        con.close();
    }

    public void setActive(int id, boolean active) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("UPDATE accounts SET active=? WHERE id=?");
        stmt.setBoolean(1, active);
        stmt.setInt(2, id);

        stmt.execute();
        con.close();
    }

    public void addDirectory(String name) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("INSERT INTO directories(name) VALUES(?)");
        stmt.setString(1, name);

        stmt.execute();
        con.close();
    }

    public Optional<Directory> getDirectoryByName(String name) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("SELECT * FROM directories WHERE name=?");
        stmt.setString(1, name);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("id");
            String name1 = rs.getString("name");

            con.close();

            if (name1 == null || name1.isBlank()) {
                System.err.println("Failed to load account \"" + id + "\" from database");
                return Optional.empty();
            }

            return Optional.of(new Directory(id, name1));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Directory> getDirectoryByID(int id) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("SELECT * FROM directories WHERE id=?");
        stmt.setInt(1, id);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int id1 = rs.getInt("id");
            String name = rs.getString("name");

            con.close();

            if (name == null || name.isBlank()) {
                System.err.println("Failed to load account \"" + id + "\" from database");
                return Optional.empty();
            }

            return Optional.of(new Directory(id1, name));
        } else {
            return Optional.empty();
        }
    }

    public List<Directory> getAllDirectories() throws Exception {
        List<Directory> directories = new ArrayList<>();

        Connection con = createConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM directories");

        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");

            if (name == null || name.isBlank()) {
                continue;
            }

            directories.add(new Directory(id, name));
        }

        con.close();

        return directories;
    }

    public void changeDirectoryName(int id, String newName) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("UPDATE directories SET name=? WHERE id=?");
        stmt.setString(1, newName);
        stmt.setInt(2, id);

        stmt.execute();
        con.close();
    }

    public void deleteDirectory(int id) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("DELETE FROM directories WHERE id=?");
        stmt.setInt(1, id);

        stmt.execute();
        con.close();
    }

    public void addContact(int directoryId, String name, String telephone, String mobile, String other, String group) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("INSERT INTO contacts(directory_id, name, telephone, mobile, other, ring, groupName) VALUES(?, ?, ?, ?, ?, ?, ?)");
        stmt.setInt(1, directoryId);
        stmt.setString(2, name);
        stmt.setString(3, telephone);
        stmt.setString(4, mobile);
        stmt.setString(5, other);
        // TODO: Implement ring
        stmt.setInt(6, 0);
        stmt.setString(7, group);

        stmt.execute();
        con.close();
    }

    public Optional<Contact> getContactByName(String name) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("SELECT * FROM contacts WHERE name=?");
        stmt.setString(1, name);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("id");
            int dirId = rs.getInt("directory_id");
            String name1 = rs.getString("name");
            String telephone = rs.getString("telephone");
            String mobile = rs.getString("mobile");
            String other = rs.getString("other");
            int ring = rs.getInt("ring");
            String group = rs.getString("groupName");

            con.close();

            if (Util.hasNull(dirId, name1, telephone, mobile, other, ring, group)) {
                System.err.println("Failed to load contact \"" + id + "\" from database");
                return Optional.empty();
            }

            return Optional.of(new Contact(id, dirId, name1, telephone, mobile, other, ring, group));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Contact> getContactByID(int id) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("SELECT * FROM contacts WHERE id=?");
        stmt.setInt(1, id);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int id1 = rs.getInt("id");
            int dirId = rs.getInt("directory_id");
            String name1 = rs.getString("name");
            String telephone = rs.getString("telephone");
            String mobile = rs.getString("mobile");
            String other = rs.getString("other");
            int ring = rs.getInt("ring");
            String group = rs.getString("groupName");

            con.close();

            if (Util.hasNull(dirId, name1, telephone, mobile, other, ring, group)) {
                System.err.println("Failed to load contact \"" + id + "\" from database");
                return Optional.empty();
            }

            return Optional.of(new Contact(id1, dirId, name1, telephone, mobile, other, ring, group));
        } else {
            return Optional.empty();
        }
    }

    public List<Contact> getAllContacts() throws Exception {
        List<Contact> contacts = new ArrayList<>();

        Connection con = createConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM contacts");

        while (rs.next()) {
            int id1 = rs.getInt("id");
            int dirId = rs.getInt("directory_id");
            String name1 = rs.getString("name");
            String telephone = rs.getString("telephone");
            String mobile = rs.getString("mobile");
            String other = rs.getString("other");
            int ring = rs.getInt("ring");
            String group = rs.getString("groupName");

            if (Util.hasNull(dirId, name1, telephone, mobile, other, ring, group)) {
                continue;
            }

            contacts.add(new Contact(id1, dirId, name1, telephone, mobile, other, ring, group));
        }

        con.close();

        return contacts;
    }

    public List<Contact> getAllContactsInDirectory(int directoryId) throws Exception {
        List<Contact> contacts = new ArrayList<>();

        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("SELECT * FROM contacts WHERE directory_id=?");
        stmt.setInt(1, directoryId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int id1 = rs.getInt("id");
            int dirId = rs.getInt("directory_id");
            String name1 = rs.getString("name");
            String telephone = rs.getString("telephone");
            String mobile = rs.getString("mobile");
            String other = rs.getString("other");
            int ring = rs.getInt("ring");
            String group = rs.getString("groupName");

            if (Util.hasNull(dirId, name1, telephone, mobile, other, ring, group)) {
                continue;
            }

            contacts.add(new Contact(id1, dirId, name1, telephone, mobile, other, ring, group));
        }

        con.close();

        return contacts;
    }

    public void changeContactName(int id, String newName) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("UPDATE contacts SET name=? WHERE id=?");
        stmt.setString(1, newName);
        stmt.setInt(2, id);

        stmt.execute();
        con.close();
    }

    public void changeContactTelephone(int id, String newPhone) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("UPDATE contacts SET telephone=? WHERE id=?");
        stmt.setString(1, newPhone);
        stmt.setInt(2, id);

        stmt.execute();
        con.close();
    }

    public void changeContactMobile(int id, String newMobile) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("UPDATE contacts SET mobile=? WHERE id=?");
        stmt.setString(1, newMobile);
        stmt.setInt(2, id);

        stmt.execute();
        con.close();
    }

    public void changeContactOther(int id, String newOther) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("UPDATE contacts SET other=? WHERE id=?");
        stmt.setString(1, newOther);
        stmt.setInt(2, id);

        stmt.execute();
        con.close();
    }

    public void changeContactGroup(int id, String newGroup) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("UPDATE contacts SET groupName=? WHERE id=?");
        stmt.setString(1, newGroup);
        stmt.setInt(2, id);

        stmt.execute();
        con.close();
    }

    public void deleteContact(int id) throws Exception {
        Connection con = createConnection();
        PreparedStatement stmt = con.prepareStatement("DELETE FROM contacts WHERE id=?");
        stmt.setInt(1, id);

        stmt.execute();
        con.close();
    }
}
