package dev.truewinter.jphonebookserver.routes;

import dev.truewinter.jphonebookserver.Util;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditContactRoute extends Route {
    @Override
    public void get(Context ctx) {
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

                        render(ctx, "edit-contact", d);
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

    @Override
    public void post(Context ctx) {
        try {
            final int dirId = Integer.parseInt(ctx.pathParam("dirId"));
            final int contactId = Integer.parseInt(ctx.pathParam("contactId"));

            getDatabase().getDirectoryByID(dirId).ifPresentOrElse(directory -> {
                HashMap<String, String> d = new HashMap<>();
                d.put("dir_id", String.valueOf(directory.getId()));
                d.put("dir_name", directory.getName());

                try {
                    getDatabase().getContactByID(contactId).ifPresentOrElse(contact -> {
                        d.put("contact_id", String.valueOf(contact.getId()));
                        d.put("contact_name", contact.getName());
                        d.put("contact_telephone", contact.getTelephone());
                        d.put("contact_mobile", contact.getMobile());
                        d.put("contact_other", contact.getOther());
                        d.put("contact_group", contact.getGroupName());

                        String newName = ctx.formParam("name");
                        String newTelephone = ctx.formParam("telephone");
                        String newMobile = ctx.formParam("mobile");
                        String newOther = ctx.formParam("other");
                        String newGroup = ctx.formParam("group");

                        if (!validate(ctx, d)) {
                            return;
                        }

                        if (!contact.getName().equals(newName)) {
                            try {
                                getDatabase().changeContactName(contact.getId(), newName);
                            } catch (Exception e) {
                                System.err.println("An error occurred while updating contact name");
                                e.printStackTrace();
                                showError(ctx, d, "An error occurred while updating contact name");
                                return;
                            }
                        }

                        if (!contact.getTelephone().equals(newTelephone)) {
                            try {
                                getDatabase().changeContactTelephone(contact.getId(), newTelephone);
                            } catch (Exception e) {
                                System.err.println("An error occurred while updating contact telephone");
                                e.printStackTrace();
                                showError(ctx, d, "An error occurred while updating contact telephone");
                                return;
                            }
                        }

                        if (!contact.getMobile().equals(newMobile)) {
                            try {
                                getDatabase().changeContactMobile(contact.getId(), newMobile);
                            } catch (Exception e) {
                                System.err.println("An error occurred while updating contact mobile");
                                e.printStackTrace();
                                showError(ctx, d, "An error occurred while updating contact mobile");
                                return;
                            }
                        }

                        if (!contact.getOther().equals(newOther)) {
                            try {
                                getDatabase().changeContactOther(contact.getId(), newOther);
                            } catch (Exception e) {
                                System.err.println("An error occurred while updating contact other");
                                e.printStackTrace();
                                showError(ctx, d, "An error occurred while updating contact other");
                                return;
                            }
                        }

                        if (!contact.getGroupName().equals(newGroup)) {
                            try {
                                getDatabase().changeContactGroup(contact.getId(), newGroup);
                            } catch (Exception e) {
                                System.err.println("An error occurred while updating contact group");
                                e.printStackTrace();
                                showError(ctx, d, "An error occurred while updating contact group");
                                return;
                            }
                        }

                        d.put("showURL", "/admin/directories/" + directory.getId() + "/contacts");
                        renderSuccess(ctx, "contacts", "Edited contact", d);
                    }, () -> {
                        d.put("showURL", "/admin/directories/" + directory.getId() + "/contacts");
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
        renderError(ctx, "edit-contact", error, data);
    }

    private boolean validPhone(String phone) {
        Pattern pattern = Pattern.compile("^\\d+$");
        Matcher matcher = pattern.matcher(phone);

        return matcher.find();
    }
}
