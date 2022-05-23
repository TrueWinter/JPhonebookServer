package dev.truewinter.jphonebookserver.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.truewinter.jphonebookserver.Contact;
import io.javalin.http.Context;

import java.util.List;

public class ContactsAPIRoute extends APIRoute {
    @Override
    public void get(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("dirId"));

            if (getDatabase().getDirectoryByID(id).isEmpty()) {
                generateError(ctx, "Directory does not exist");
                return;
            }

            List<Contact> contacts = getDatabase().getAllContactsInDirectory(id);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("success", true);
            ArrayNode arrayNode = mapper.createArrayNode();

            for (Contact contact : contacts) {
                ObjectNode directoryNode = mapper.createObjectNode();
                directoryNode.put("id", contact.getId());
                directoryNode.put("name", contact.getName());
                directoryNode.put("telephone", contact.getTelephone());
                directoryNode.put("mobile", contact.getMobile());
                directoryNode.put("other", contact.getOther());
                directoryNode.put("group", contact.getGroupName());
                arrayNode.add(directoryNode);
            }

            node.set("contacts", arrayNode);
            ctx.json(node);
        } catch (NumberFormatException e) {
            generateError(ctx, "Invalid directory ID");
        } catch (Exception e) {
            System.err.println("An error occurred while retrieving contacts");
            e.printStackTrace();

            generateError(ctx, "An error occurred while retrieving contacts");
        }
    }
}
