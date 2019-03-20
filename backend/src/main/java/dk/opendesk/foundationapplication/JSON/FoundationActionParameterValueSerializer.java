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

        jgen.writeStartObject();
        FoundationActionParameterDefinitionSerializer.writeFields(paramVal, jgen);
        if (paramVal.getValue() != null) {
            jgen.writeObjectField("value", paramVal.getValue());
        }
        jgen.writeEndObject();
    }

    @Override
    public Class<FoundationActionParameterValue> handledType() {
        return FoundationActionParameterValue.class;
    }

}
