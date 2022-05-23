package dev.truewinter.jphonebookserver.routes;

import io.javalin.http.Context;

import java.util.HashMap;

public class ContactsRoute extends Route {
    @Override
    public void get(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            getDatabase().getDirectoryByID(id).ifPresentOrElse(directory -> {
                HashMap<String, String> d = new HashMap<>();
                d.put("dir_id", String.valueOf(directory.getId()));
                d.put("dir_name", directory.getName());
                render(ctx, "contacts", d);
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
}
