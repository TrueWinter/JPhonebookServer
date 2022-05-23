package dev.truewinter.jphonebookserver;

import io.javalin.http.Context;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;

public class Util {
    // https://stackoverflow.com/a/15954821
    public static Path getInstallPath() {
        Path relative = Paths.get("");
        return relative.toAbsolutePath();
    }

    // https://stackoverflow.com/a/16911389
    public static boolean hasNull(Object... args) {
        for (Object arg : args) {
            if (arg == null) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasBlank(String... args) {
        for (String arg : args) {
            if (arg == null) {
                return true;
            }

            if (arg.isBlank()) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasAtLeastOne(String ...args) {
        for (String arg : args) {
            if (arg != null && !arg.isBlank()) {
                return true;
            }
        }

        return false;
    }

    // https://stackoverflow.com/a/50381020
    public static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }

    public static String getURL(Context ctx) throws MalformedURLException {
        URL url = new URL(ctx.url());
        String outUrl = url.getProtocol() +
                "://" +
                ctx.host() +
                "/";

        return outUrl;
    }
    public static String getVersion() throws IOException {
        Properties properties = new Properties();
        properties.load(Util.class.getClassLoader().getResourceAsStream("jphonebookserver.properties"));
        return properties.getProperty("version");
    }

}
