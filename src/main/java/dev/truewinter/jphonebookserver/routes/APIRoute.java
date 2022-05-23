package dev.truewinter.jphonebookserver.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.truewinter.jphonebookserver.AccountRoles;
import io.javalin.http.Context;

public abstract class APIRoute extends Route {
    public Route verifyLogin(Context ctx) {
        if (!isLoggedIn(ctx)) {
            generateError(ctx, "You must be logged in to access this route");
            return new NoOpAPIRoute();
        }

        return this;
    }

    public Route requireAdmin(Context ctx) {
        String role = ctx.sessionAttribute("logged-in-user-role");

        if (!AccountRoles.ADMIN.equalsRole(role)) {
            generateError(ctx, "You do not have the role required to access this route");
            return new NoOpAPIRoute();
        }

        return this;
    }

    protected void generateError(Context ctx, String error) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("success", false);
        node.put("error", error);
        ctx.json(node);
    }

    public static class NoOpAPIRoute extends NoOpRoute {
        @Override
        public void get(Context ctx) {}

        @Override
        public void post(Context ctx) {}
    }
}
