package dev.truewinter.jphonebookserver;

public class Contact {
    private int id;
    private int directoryId;
    private String name;
    private String telephone;
    private String mobile;
    private String other;
    private int ring;
    private String groupName;

    public Contact(int id, int directoryId, String name, String telephone, String mobile, String other, int ring, String groupName) {
        this.id = id;
        this.directoryId = directoryId;
        this.name = name;
        this.telephone = telephone;
        this.mobile = mobile;
        this.other = other;
        this.ring = ring;
        this.groupName = groupName;
    }

    public int getId() {
        return id;
    }

    public int getDirectoryId() {
        return directoryId;
    }

    public String getName() {
        return name;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getMobile() {
        return mobile;
    }

    public String getOther() {
        return other;
    }

    public int getRing() {
        return ring;
    }

    public String getGroupName() {
        return groupName;
    }
}
