package dev.truewinter.jphonebookserver.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.truewinter.jphonebookserver.Account;
import io.javalin.http.Context;

import java.util.List;

public class AccountsAPIRoute extends APIRoute {
    @Override
    public void get(Context ctx) {
        try {
            List<Account> accounts = getDatabase().getAllAccounts();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("success", true);
            ArrayNode arrayNode = mapper.createArrayNode();

            for (Account account : accounts) {
                ObjectNode accountNode = mapper.createObjectNode();
                accountNode.put("id", account.getId());
                accountNode.put("username", account.getUsername());
                accountNode.put("role", account.getRole().toString());
                accountNode.put("active", account.isActive());
                arrayNode.add(accountNode);
            }

            node.set("accounts", arrayNode);
            ctx.json(node);
        } catch (Exception e) {
            System.err.println("An error occurred while retrieving accounts");
            e.printStackTrace();

            generateError(ctx, "An error occurred while retrieving accounts");
        }
    }
}
