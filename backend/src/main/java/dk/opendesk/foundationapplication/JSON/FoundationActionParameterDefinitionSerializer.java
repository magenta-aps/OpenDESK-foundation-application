package dk.opendesk.foundationapplication.JSON;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dk.opendesk.foundationapplication.DAO.FoundationActionParameterDefinition;

import java.io.IOException;

public class FoundationActionParameterDefinitionSerializer extends JsonSerializer<FoundationActionParameterDefinition> {

    @Override
    public void serialize(FoundationActionParameterDefinition paramDef, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        writeFields(paramDef, jgen);
        jgen.writeEndObject();
    }

    public static void writeFields(FoundationActionParameterDefinition paramDef, JsonGenerator jgen) throws IOException {
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
        jgen.writeBooleanField("isMultivalued", paramDef.isMultiValued());
        jgen.writeBooleanField("isMandatory", paramDef.isMandatory());

    }
    @Override
    public Class<FoundationActionParameterDefinition> handledType() {
        return FoundationActionParameterDefinition.class;
    }
}
