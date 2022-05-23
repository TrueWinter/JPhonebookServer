package dev.truewinter.jphonebookserver.routes;

import io.javalin.http.Context;

import java.util.HashMap;

public class DeleteDirectoryRoute extends Route {
    @Override
    public void post(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            HashMap<String, String> d = new HashMap<>();
            d.put("showURL", "/admin/directories");

            getDatabase().getDirectoryByID(id).ifPresentOrElse(directory -> {
                try {
                    if (!getDatabase().getAllContactsInDirectory(directory.getId()).isEmpty()) {
                        renderError(ctx, "directories", "You can only delete directories that contain no contacts", d);
                        return;
                    }

                    getDatabase().deleteDirectory(directory.getId());

                    renderSuccess(ctx, "directories", "Deleted directory", d);
                } catch (Exception e) {
                    System.err.println("Failed to delete directory from database");
                    e.printStackTrace();
                    renderError(ctx, "directories", "Failed to delete directory from database", d);
                }
            }, () -> {
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
