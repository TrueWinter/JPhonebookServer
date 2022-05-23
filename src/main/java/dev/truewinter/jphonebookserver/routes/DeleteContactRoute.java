package dev.truewinter.jphonebookserver.routes;

import io.javalin.http.Context;

import java.util.HashMap;

public class DeleteContactRoute extends Route {
    @Override
    public void post(Context ctx) {
        try {
            final int dirId = Integer.parseInt(ctx.pathParam("dirId"));
            final int contactId = Integer.parseInt(ctx.pathParam("contactId"));

            getDatabase().getDirectoryByID(dirId).ifPresentOrElse(directory -> {
                HashMap<String, String> d = new HashMap<>();
                d.put("dir_id", String.valueOf(directory.getId()));
                d.put("dir_name", directory.getName());
                d.put("showURL", "/admin/directories/" + directory.getId() + "/contacts");

                try {
                    getDatabase().getContactByID(contactId).ifPresentOrElse(contact -> {
                        d.put("contact_id", String.valueOf(contact.getId()));
                        d.put("contact_name", contact.getName());
                        d.put("contact_telephone", contact.getTelephone());
                        d.put("contact_mobile", contact.getMobile());
                        d.put("contact_other", contact.getOther());
                        d.put("contact_group", contact.getGroupName());

                        try {
                            getDatabase().deleteContact(contactId);
                            renderSuccess(ctx, "contacts", "Contact deleted", d);
                        } catch (Exception e) {
                            System.err.println("Failed to delete contact from database");
                            e.printStackTrace();
                            renderError(ctx, "contacts", "Failed to delete contact from database", d);
                        }
                    }, () -> {
                        renderError(ctx, "contacts", "Failed to load directory data from database", d);
                    });
                } catch (NumberFormatException e) {
                    renderError(ctx, "error", "Invalid contact ID");
                } catch (Exception e) {
                    System.err.println("Failed to load contact data from database");
                    e.printStackTrace();
                    renderError(ctx, "error", "Failed to load contact data from database");
                }
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
