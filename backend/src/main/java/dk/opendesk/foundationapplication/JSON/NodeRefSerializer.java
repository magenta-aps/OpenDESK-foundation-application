package dk.opendesk.foundationapplication.JSON;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.IOException;

public class NodeRefSerializer extends JsonSerializer<NodeRef> {
    @Override
    public void serialize(NodeRef nodeRef, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        if (nodeRef.getId() != null) {
            jgen.writeStringField("id", nodeRef.getId());
        }
        if (nodeRef.getStoreRef() != null) {
            jgen.writeStringField("storeRef", nodeRef.getStoreRef().toString());
        }
        jgen.writeEndObject();
    }

    @Override
    public Class handledType() {
        return NodeRef.class;
    }
}
