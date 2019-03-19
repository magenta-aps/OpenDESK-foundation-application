package dk.opendesk.foundationapplication.JSON;

import dk.opendesk.foundationapplication.DAO.FoundationActionParameterDefinition;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public class FoundationActionParameterDefinitionSerializer extends JsonSerializer<FoundationActionParameterDefinition> {

    @Override
    public void serialize(FoundationActionParameterDefinition paramDef, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        if (paramDef.getName() != null) {
            jgen.writeStringField("name", paramDef.getName());
        }
        if (paramDef.getType() != null) {
            jgen.writeStringField("type", paramDef.getType().getLocalName());
        }
        if (paramDef.getDisplayLabel() != null) {
            jgen.writeStringField("displayLabel", paramDef.getDisplayLabel());
        }
        if (paramDef.getParameterConstraintName() != null) {
            jgen.writeStringField("parameterConstraintName", paramDef.getParameterConstraintName());
        }
        if (paramDef.getJavaType() != null) {
            jgen.writeStringField("javaType", paramDef.getJavaType().getCanonicalName());
        }
        jgen.writeStringField("isMultivalued", paramDef.isMultiValued() ? "true" : "false");
        jgen.writeStringField("isMandatory", paramDef.isMandatory() ? "true" : "false");
        jgen.writeEndObject();
    }

    @Override
    public Class<FoundationActionParameterDefinition> handledType() {
        return FoundationActionParameterDefinition.class;
    }
}
