package dev.truewinter.jphonebookserver;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Config {
    private YamlDocument config;
    private int port;
    private boolean auth;
    private String username;
    private String password;

    public void loadConfig(File configFile) throws IOException {
        config = YamlDocument.create(
                configFile,
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("config.yml")),
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build()
        );

        port = config.getInt("port");
        auth = config.getBoolean("auth");
        username = config.getString("username");
        password = config.getString("password");
    }

    public int getPort() {
        return port;
    }

    public boolean isAuthEnabled() {
        return auth;
    }

    public boolean isValidLogin(String user, String pass) {
        return user.equals(username) && pass.equals(password);
    }
}
