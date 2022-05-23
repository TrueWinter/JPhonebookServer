package dev.truewinter.jphonebookserver.routes;

import dev.truewinter.jphonebookserver.Account;
import dev.truewinter.jphonebookserver.AccountRoles;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Objects;

public class EditAccountRoute extends Route {
    @Override
    public void get(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("id"));

            if (!AccountRoles.ADMIN.equalsRole(ctx.sessionAttribute("logged-in-user-role"))
                    && !Objects.equals(ctx.sessionAttribute("logged-in-user"), userId)) {
                renderError(ctx, "error", "You do not have permission to view that account");
                return;
            }

            getDatabase().getAccountByID(userId).ifPresentOrElse(account -> {
                render(ctx, "edit-account", createAccountMap(account));
            }, () -> {
                HashMap<String, String> d = new HashMap<>();
                d.put("showURL", "/admin/accounts");
                renderError(ctx, "accounts", "Failed to load account data from database", d);
            });
        } catch (NumberFormatException e) {
            renderError(ctx, "error", "Invalid user ID");
        } catch (Exception e) {
            System.err.println("Failed to load account data from database");
            e.printStackTrace();
            renderError(ctx, "error", "Failed to load account data from database");
        }
    }

    @Override
    public void post(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("id"));

            if (!AccountRoles.ADMIN.equalsRole(ctx.sessionAttribute("logged-in-user-role"))
                    && !Objects.equals(ctx.sessionAttribute("logged-in-user"), userId)) {
                renderError(ctx, "error", "You do not have permission to view that account");
                return;
            }

            getDatabase().getAccountByID(userId).ifPresentOrElse(account -> {
                HashMap<String, String> a = new HashMap<>();
                int accId = account.getId();
                String accUsername = account.getUsername();
                AccountRoles accRole = account.getRole();
                boolean accActive = account.isActive();

                int id = ctx.sessionAttribute("logged-in-user");
                String username = ctx.formParam("username");
                String password = ctx.formParam("password");
                String confirmPassword = ctx.formParam("confirmPassword");
                AccountRoles role = AccountRoles.fromString(ctx.formParam("role"));
                boolean active = false;

                if (ctx.formParam("active") != null && ctx.formParam("active").toString().equals("on")) {
                    active = true;
                }

                if (accId == id) {
                    active = true;
                }

                if (confirmPassword != null && !confirmPassword.isBlank()) {
                    if (password == null || password.isBlank()) {
                        renderError(ctx, "edit-account", "Passwords do not match", createAccountMap(account));
                        return;
                    }
                }

                if (password != null && !password.isBlank()) {
                    if (!password.equals(confirmPassword)) {
                        renderError(ctx, "edit-account", "Passwords do not match", createAccountMap(account));
                        return;
                    }

                    if (password.length() > 72) {
                        renderError(ctx, "edit-account", "The maximum password length is 72");
                        return;
                    }

                    try {
                        getDatabase().changePassword(accId, password);
                    } catch (Exception e) {
                        System.err.println("Failed to change password");
                        e.printStackTrace();
                        renderError(ctx, "edit-account", "Failed to change password", createAccountMap(account));
                        return;
                    }
                }

                if (AccountRoles.ADMIN.equalsRole(ctx.sessionAttribute("logged-in-user-role"))) {
                    if (username != null && !username.isBlank()) {
                        if (username.length() > 255) {
                            renderError(ctx, "edit-account", "Usernames can be up to 255 characters long");
                            return;
                        }

                        if (!accUsername.equals(username) && !accUsername.equals("admin")) {
                            try {
                                getDatabase().changeUsername(accId, username);
                            } catch (Exception e) {
                                renderError(ctx, "error", "Failed to load account data from database");
                            }
                        }
                    }

                    if (role != null && !role.equals(accRole)) {
                        try {
                            getDatabase().changeRole(accId, role);
                        } catch (Exception e) {
                            renderError(ctx, "error", "Failed to load account data from database");
                        }
                    }

                    if (accActive != active) {
                        try {
                            getDatabase().setActive(accId, active);
                        } catch (Exception e) {
                            renderError(ctx, "error", "Failed to load account data from database");
                        }
                    }
                }

                try {
                    getDatabase().getAccountByID(userId).ifPresent(acc -> {
                        renderSuccess(ctx,
                                "edit-account",
                                "Account edited; the user may need to log out and back in again for changes to take effect",
                                createAccountMap(acc)
                        );
                    });
                } catch (Exception e) {
                    renderError(ctx, "error", "Failed to load account data from database");
                }
            }, () -> {
                renderError(ctx, "error", "Failed to load account data from database");
            });
        } catch (NumberFormatException e) {
            renderError(ctx, "error", "Invalid user ID");
        } catch (Exception e) {
            System.err.println("Failed to load account data from database");
            e.printStackTrace();
            renderError(ctx, "error", "Failed to load account data from database");
        }
    }

    private HashMap<String, String> createAccountMap(Account account) {
        HashMap<String, String> a = new HashMap<>();
        a.put("acc_id", String.valueOf(account.getId()));
        a.put("acc_username", account.getUsername());
        a.put("acc_role", account.getRole().toString());
        a.put("acc_active", String.valueOf(account.isActive()));

        return a;
    }
}
