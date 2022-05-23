package dev.truewinter.jphonebookserver.routes;

import io.javalin.http.Context;

import java.util.HashMap;

public class AddDirectoryRoute extends Route {
    @Override
    public void get(Context ctx) {
        render(ctx, "add-directory");
    }

    @Override
    public void post(Context ctx) {
        String name = ctx.formParam("name");

        if (name == null || name.isBlank()) {
            renderError(ctx, "add-directory", "Name is required");
            return;
        }

        if (name.length() > 255) {
            renderError(ctx, "add-directory", "Name is required");
            return;
        }

        if (name.contains(" ")) {
            renderError(ctx, "add-directory", "Name cannot contain spaces");
            return;
        }

        try {
            if (getDatabase().getDirectoryByName(name).isPresent()) {
                renderError(ctx, "add-directory", "Directory with that name already exists");
                return;
            }

            getDatabase().addDirectory(name);
            HashMap<String, String> d = new HashMap<>();
            d.put("showURL", "/admin/directories");
            renderSuccess(ctx, "directories", "Added directory", d);
        } catch (Exception e) {
            renderError(ctx, "error", "Failed to query database");
        }
    }
}
