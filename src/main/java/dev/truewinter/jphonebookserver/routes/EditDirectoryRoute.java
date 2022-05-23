package dev.truewinter.jphonebookserver.routes;

import dev.truewinter.jphonebookserver.Directory;
import io.javalin.http.Context;

import java.util.HashMap;

public class EditDirectoryRoute extends Route {
    @Override
    public void get(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            getDatabase().getDirectoryByID(id).ifPresentOrElse(directory -> {
                render(ctx, "edit-directory", createDirectoryMap(directory));
            }, () -> {
                HashMap<String, String> d = new HashMap<>();
                d.put("showURL", "/admin/directories");
                renderError(ctx, "directories", "Failed to load directory data from database", d);
            });
        } catch (NumberFormatException e) {
            renderError(ctx, "error", "Invalid directory ID");
        } catch (Exception e) {
            System.err.println("Failed to load directory data from database");
            e.printStackTrace();
            renderError(ctx, "error", "Failed to load directory data from database");
        }
    }

    @Override
    public void post(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            getDatabase().getDirectoryByID(id).ifPresentOrElse(directory -> {
                String name = ctx.formParam("name");

                if (name == null || name.isBlank()) {
                    renderError(ctx, "edit-directory", "Name is required", createDirectoryMap(directory));
                    return;
                }

                if (name.length() > 255) {
                    renderError(ctx, "edit-directory", "Name is required", createDirectoryMap(directory));
                    return;
                }

                if (name.contains(" ")) {
                    renderError(ctx, "edit-directory", "Name cannot contain spaces", createDirectoryMap(directory));
                    return;
                }

                try {
                    getDatabase().changeDirectoryName(id, name);
                } catch (Exception e) {
                    System.err.println("Failed to change directory name");
                    e.printStackTrace();
                    renderError(ctx, "edit-directory", "Failed to change directory name", createDirectoryMap(directory));
                }

                HashMap<String, String> d = new HashMap<>();
                d.put("showURL", "/admin/directories");
                renderSuccess(ctx, "directories", "Edited directory", d);
            }, () -> {
                renderError(ctx, "error", "Failed to load directory data from database");
            });
        } catch (NumberFormatException e) {
            renderError(ctx, "error", "Invalid directory ID");
        } catch (Exception e) {
            System.err.println("Failed to load directory data from database");
            e.printStackTrace();
            renderError(ctx, "error", "Failed to load directory data from database");
        }
    }

    private HashMap<String, String> createDirectoryMap(Directory directory) {
        HashMap<String, String> d = new HashMap<>();
        d.put("dir_id", String.valueOf(directory.getId()));
        d.put("dir_name", directory.getName());

        return d;
    }
}
