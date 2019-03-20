package dk.opendesk.foundationapplication.JSON;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dk.opendesk.foundationapplication.DAO.FoundationActionParameterValue;

import java.io.IOException;

public class FoundationActionParameterValueSerializer extends JsonSerializer<FoundationActionParameterValue> {

    @Override
    public void serialize(FoundationActionParameterValue paramVal, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        //todo evt lave metode så jeg kan genbruge fra foundationActionParameterDefinitionSerializer
        jgen.writeStartObject();
        if (paramVal.getName() != null) {
            jgen.writeStringField("name", paramVal.getName());
        }
        if (paramVal.getType() != null) {
            jgen.writeStringField("type", paramVal.getType().getLocalName());
        }
        if (paramVal.getDisplayLabel() != null) {
            jgen.writeStringField("displayLabel", paramVal.getDisplayLabel());
        }
        if (paramVal.getParameterConstraintName() != null) {
            jgen.writeStringField("parameterConstraintName", paramVal.getParameterConstraintName());
        }
        if (paramVal.getJavaType() != null) {
            jgen.writeStringField("javaType", paramVal.getJavaType().getCanonicalName());
        }
        if (paramVal.getValue() != null) {
            jgen.writeObjectField("value", paramVal.getValue());
        }
        jgen.writeBooleanField("isMultiValued", paramVal.isMultiValued());
        jgen.writeBooleanField("isMandatory", paramVal.isMandatory());
        jgen.writeEndObject();
    }

    @Override
    public Class<FoundationActionParameterValue> handledType() {
        return FoundationActionParameterValue.class;
    }

}
