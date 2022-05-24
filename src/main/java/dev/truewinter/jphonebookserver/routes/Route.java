package dev.truewinter.jphonebookserver.routes;

import dev.truewinter.jphonebookserver.AccountRoles;
import dev.truewinter.jphonebookserver.Database;
import dev.truewinter.jphonebookserver.Util;
import io.javalin.http.Context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public abstract class Route {
    private static final String WEB_ROOT = "web/html/";
    private static final String EXTENSION = ".peb";
    private static Database database;

    public void get(Context ctx) {}

    public void post(Context ctx) {}

    public Route verifyLogin(Context ctx) {
        if (!isLoggedIn(ctx)) {
            ctx.redirect("/admin/login");
            return new NoOpRoute();
        }

        return this;
    }

    public Route requireAdmin(Context ctx) {
        String role = ctx.sessionAttribute("logged-in-user-role");

        if (!AccountRoles.ADMIN.equalsRole(role)) {
            renderError(ctx, "error", "You do not have the role required to access that page");
            return new NoOpRoute();
        }

        return this;
    }

    public Route verifyCSRF(Context ctx, String pageToRenderOnError) {
        String csrf = ctx.formParam("csrf");

        if (csrf == null || !csrf.equals(Objects.requireNonNull(ctx.sessionAttribute("csrf")).toString())) {
            renderError(ctx, pageToRenderOnError, "CSRF token is invalid");
            return new NoOpRoute();
        }

        return this;
    }

    private void addDefaultModelValues(Context ctx, HashMap<String, String> model) {
        if (ctx.sessionAttribute("logged-in-user") != null) {
            // The ID must be a string here as I can't see any way to convert an int to a string in Pebble
            model.put("id", ctx.sessionAttribute("logged-in-user").toString());
            model.put("role", ctx.sessionAttribute("logged-in-user-role"));
            model.put("csrf", ctx.sessionAttribute("csrf"));
        }

        try {
            model.put("JPhonebookServerVersion", Util.getVersion());
        } catch (IOException e) {
            e.printStackTrace();
            model.put("JPhonebookServerVersion", "x.x.x");
        }
    }

    public void render(Context ctx, String path) {
        render(ctx, path, new HashMap<>());
    }

    public void render(Context ctx, String path, HashMap<String, String> model) {
        addDefaultModelValues(ctx, model);
        ctx.render(getPath(path), model);
    }

    public void renderError(Context ctx, String path, String error) {
        renderError(ctx, path, error, new HashMap<>());
    }

    public void renderError(Context ctx, String path, String error, HashMap<String, String> model) {
        addDefaultModelValues(ctx, model);
        model.put("error", error);
        ctx.render(getPath(path), model);
    }

    public void renderSuccess(Context ctx, String path, String success) {
        renderSuccess(ctx, path, success, new HashMap<>());
    }

    public void renderSuccess(Context ctx, String path, String success, HashMap<String, String> model) {
        addDefaultModelValues(ctx, model);
        model.put("success", success);
        ctx.render(getPath(path), model);
    }

    public static String getPath(String file) {
        return WEB_ROOT + file + EXTENSION;
    }

    public static void setDatabaseInstance(Database db) {
        database = db;
    }

    protected Database getDatabase() {
        return database;
    }

    protected boolean isLoggedIn(Context ctx) {
        return ctx.sessionAttribute("logged-in-user") != null;
    }

    public static class NoOpRoute extends Route {
        @Override
        public void get(Context ctx) {}

        @Override
        public void post(Context ctx) {}

        public Route verifyLogin(Context ctx) {
            return this;
        }

        public Route requireAdmin(Context ctx) {
            return this;
        }

        public Route verifyCSRF(Context ctx, String pageToRenderOnError) {
            return this;
        }
    }
}
