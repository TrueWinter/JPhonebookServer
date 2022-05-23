package dev.truewinter.jphonebookserver.routes;

import dev.truewinter.jphonebookserver.Util;
import io.javalin.http.Context;

public class LoginRoute extends Route {
    @Override
    public void get(Context ctx) {
        if (isLoggedIn(ctx)) {
            ctx.redirect("/admin/directories");
            return;
        }

        render(ctx, "login");
    }

    @Override
    public void post(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");

        if (username == null || password == null) {
            renderError(ctx, "login", "Username and password is required");
            return;
        }

        try {
            getDatabase().getAccountByUsernameIfPasswordIsCorrect(username, password).ifPresentOrElse(account -> {
                if (!account.isActive()) {
                    renderError(ctx, "login", "Account is not active");
                    return;
                }

                ctx.sessionAttribute("logged-in-user", account.getId());
                ctx.sessionAttribute("logged-in-user-role", account.getRole().toString());
                ctx.sessionAttribute("csrf", Util.generateRandomString(16));

                ctx.redirect("/admin/directories");
            }, () -> {
                renderError(ctx, "login", "Username or password is incorrect");
            });
        } catch (Exception e) {
            System.err.println("An error occurred while fetching data from database.");
            e.printStackTrace();
            renderError(ctx, "login", "An error occurred while fetching data from database");
        }
    }
}
