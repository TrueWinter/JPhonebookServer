package dev.truewinter.jphonebookserver.routes;

import io.javalin.http.Context;

public class AccountsRoute extends Route {
    @Override
    public void get(Context ctx) {
        render(ctx, "accounts");
    }
}
