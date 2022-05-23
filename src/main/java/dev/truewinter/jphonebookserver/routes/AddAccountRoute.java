package dev.truewinter.jphonebookserver.routes;

import dev.truewinter.jphonebookserver.AccountRoles;
import dev.truewinter.jphonebookserver.Util;
import io.javalin.http.Context;

import java.util.HashMap;

public class AddAccountRoute extends Route {
    @Override
    public void get(Context ctx) {
        render(ctx, "add-account");
    }

    @Override
    public void post(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        String confirmPassword = ctx.formParam("confirmPassword");
        String role = ctx.formParam("role");

        if (Util.hasBlank(username, password, confirmPassword, role)) {
            renderError(ctx, "add-account", "All fields are required");
            return;
        }

        if (username.length() > 255) {
            renderError(ctx, "add-account", "Usernames can be up to 255 characters long");
            return;
        }

        if (!password.equals(confirmPassword)) {
            renderError(ctx, "add-account", "Passwords do not match");
            return;
        }

        if (password.length() > 72) {
            renderError(ctx, "add-account", "The maximum password length is 72");
            return;
        }

        if (AccountRoles.fromString(role) == null) {
            renderError(ctx, "add-account", "Invalid role");
            return;
        }

        try {
            if (getDatabase().getAccountByUsername(username).isPresent()) {
                renderError(ctx, "add-account", "An account with that username already exists");
                return;
            }

            getDatabase().addAccount(username, password, AccountRoles.fromString(role));
            HashMap<String, String> d = new HashMap<>();
            d.put("showURL", "/admin/accounts");
            renderSuccess(ctx, "accounts", "Account created", d);
        } catch (Exception e) {
            System.out.println("Failed to create account");
            e.printStackTrace();
            renderError(ctx, "add-account", "Failed to create account");
        }
    }
}
