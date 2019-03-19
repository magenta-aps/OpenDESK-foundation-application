package dk.opendesk.foundationapplication.JSON;

import dk.opendesk.foundationapplication.DAO.FoundationActionParameterValue;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public class FoundationActionParameterValueSerializer extends JsonSerializer<FoundationActionParameterValue> {

    @Override
    public void serialize(FoundationActionParameterValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        if (value.getName() != null) {
            jgen.writeStringField("name", value.getName());
        }
        if (value.getType() != null) {
            jgen.writeStringField("type", value.getType());
        }
        if (value.isMultivalued() != null) {
            jgen.writeStringField("isMultivalued", value.isMultivalued().toString());
        }
        if (value.isMandatory() != null) {
            jgen.writeStringField("isMandatory", value.isMandatory().toString());
        }
        if (value.getDisplayLabel() != null) {
            jgen.writeStringField("displayLabel", value.getDisplayLabel());
        }
        if (value.getParameterConstraintName() != null) {
            jgen.writeStringField("parameterConstraintName", value.getParameterConstraintName());
        }
        if (value.getValue() != null) {
            jgen.writeStringField("value", value.getValue());
        }
    }
}
