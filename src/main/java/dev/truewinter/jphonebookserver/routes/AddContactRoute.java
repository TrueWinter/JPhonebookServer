package dev.truewinter.jphonebookserver.routes;

import dev.truewinter.jphonebookserver.Util;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddContactRoute extends Route {
    @Override
    public void get(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            getDatabase().getDirectoryByID(id).ifPresentOrElse(directory -> {
                HashMap<String, String> d = new HashMap<>();
                d.put("dir_id", String.valueOf(directory.getId()));
                d.put("dir_name", directory.getName());

                render(ctx, "add-contact", d);
            }, () -> {
                renderError(ctx, "directories", "Failed to load directory data from database");
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
                HashMap<String, String> d = new HashMap<>();
                d.put("dir_id", String.valueOf(directory.getId()));
                d.put("dir_name", directory.getName());

                String name = ctx.formParam("name");
                String telephone = ctx.formParam("telephone");
                String mobile = ctx.formParam("mobile");
                String other = ctx.formParam("other");
                String group = ctx.formParam("group");

                if (!validate(ctx, d)) {
                    return;
                }

                try {
                    getDatabase().addContact(id, name, telephone, mobile, other, group);
                } catch (Exception e) {
                    System.err.println("Failed to add contact to database");
                    e.printStackTrace();
                    renderError(ctx, "add-contact", "Failed to add contact to database");
                    return;
                }

                d.put("showURL", "/admin/directories/" + directory.getId() + "/contacts");
                renderSuccess(ctx, "contacts", "Contact added", d);
            }, () -> {
                renderError(ctx, "directories", "Failed to load directory data from database");
            });
        } catch (NumberFormatException e) {
            renderError(ctx, "error", "Invalid directory ID");
        } catch (Exception e) {
            System.err.println("Failed to load directory data from database");
            e.printStackTrace();
            renderError(ctx, "error", "Failed to load directory data from database");
        }
    }

    private boolean validate(Context ctx, HashMap<String, String> data) {
        String newName = ctx.formParam("name");
        String newTelephone = ctx.formParam("telephone");
        String newMobile = ctx.formParam("mobile");
        String newOther = ctx.formParam("other");
        String newGroup = ctx.formParam("group");

        if (newName == null || newName.isBlank()) {
            showError(ctx, data, "Name is required");
            return false;
        }

        if (newName.length() > 255) {
            showError(ctx, data, "Name length is limited to 255 characters");
            return false;
        }

        if (!Util.hasAtLeastOne(newTelephone, newMobile, newOther)) {
            showError(ctx, data, "At least one of the following is required: telephone, mobile, other");
            return false;
        }

        if (newTelephone != null && !newTelephone.isBlank()) {
            if (!validPhone(newTelephone)) {
                showError(ctx, data, "Telephone must be numeric");
                return false;
            }

            if (newTelephone.length() > 20) {
                showError(ctx, data, "Telephone length is limited to 20 characters");
                return false;
            }
        }

        if (newMobile != null && !newMobile.isBlank()) {
            if (!validPhone(newMobile)) {
                showError(ctx, data, "Mobile must be numeric");
                return false;
            }

            if (newMobile.length() > 20) {
                showError(ctx, data, "Mobile length is limited to 20 characters");
                return false;
            }
        }

        if (newOther != null && !newOther.isBlank()) {
            if (!validPhone(newOther)) {
                showError(ctx, data, "Other must be numeric");
                return false;
            }

            if (newOther.length() > 20) {
                showError(ctx, data, "Other length is limited to 20 characters");
                return false;
            }
        }

        if (newGroup != null && !newGroup.isBlank()) {
            if (newGroup.length() > 30) {
                showError(ctx, data, "Group length is limited to 30 characters");
                return false;
            }
        }

        return true;
    }

    private void showError(Context ctx, HashMap<String, String> data, String error) {
        renderError(ctx, "add-contact", error, data);
    }

    private boolean validPhone(String phone) {
        Pattern pattern = Pattern.compile("^\\d+$");
        Matcher matcher = pattern.matcher(phone);

        return matcher.find();
    }
}
