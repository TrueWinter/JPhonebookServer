package dev.truewinter.jphonebookserver;

import java.io.IOException;
import java.nio.file.Path;

public class JPhonebookServer {
    private static Database database;
    private static Config config;
    private static WebServer webServer;

    public static void main(String[] args) {
        try {
            System.out.println("Starting JPhonebookServer v" + Util.getVersion());
        } catch (IOException e) {
            System.out.println("Starting JPhonebookServer (unknown version)");
        }

        try {
            database = Database.getInstance();

            Path configPath = Path.of(Util.getInstallPath().toString(), "config.yml");
            config = new Config();
            config.loadConfig(configPath.toFile());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        webServer = new WebServer(database, config);
        webServer.start();
    }

    protected static Database getDatabase() {
        return database;
    }

    public static Config getConfig() {
        return config;
    }

    public static WebServer getWebServer() {
        return webServer;
    }
}
