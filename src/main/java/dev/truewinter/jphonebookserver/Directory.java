package dev.truewinter.jphonebookserver;

public class Directory {
    private int id;
    private String name;

    public Directory(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
