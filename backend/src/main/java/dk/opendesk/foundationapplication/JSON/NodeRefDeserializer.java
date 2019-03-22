package dk.opendesk.foundationapplication.JSON;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

import java.io.IOException;

public class NodeRefDeserializer extends JsonDeserializer<NodeRef> {
    @Override
    public NodeRef deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        if (node.has("id") && node.has("storeRef")) {
            String id = node.get("id").asText();
            String storeRef = node.get("storeRef").asText();
            return new NodeRef(new StoreRef(storeRef),id);
        }
        // todo construere noderef fra id alene?
        return null;
    }

    @Override
    public Class<?> handledType() {
        return NodeRef.class;
    }
}
