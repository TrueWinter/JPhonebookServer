package dev.truewinter.jphonebookserver.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.truewinter.jphonebookserver.Directory;
import io.javalin.http.Context;

import java.util.List;

public class DirectoriesAPIRoute extends APIRoute {
    @Override
    public void get(Context ctx) {
        try {
            List<Directory> accounts = getDatabase().getAllDirectories();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("success", true);
            ArrayNode arrayNode = mapper.createArrayNode();

            for (Directory directory : accounts) {
                ObjectNode directoryNode = mapper.createObjectNode();
                directoryNode.put("id", directory.getId());
                directoryNode.put("name", directory.getName());
                arrayNode.add(directoryNode);
            }

            node.set("directories", arrayNode);
            ctx.json(node);
        } catch (Exception e) {
            System.err.println("An error occurred while retrieving directories");
            e.printStackTrace();

            generateError(ctx, "An error occurred while retrieving directories");
        }
    }
}
