package dk.opendesk.foundationapplication.JSON;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.opendesk.foundationapplication.DAO.FoundationActionParameterDefinition;
import dk.opendesk.foundationapplication.DAO.FoundationActionParameterValue;
import dk.opendesk.foundationapplication.Utilities;

import java.io.IOException;
import java.util.Date;

public class FoundationActionParameterValueDeserializer extends JsonDeserializer<FoundationActionParameterValue> {

    @Override
    public FoundationActionParameterValue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper mapper = Utilities.getMapper();
        JsonNode node = jp.getCodec().readTree(jp);
        //try {
            FoundationActionParameterDefinitionDeserializer deserializer = new FoundationActionParameterDefinitionDeserializer();
            FoundationActionParameterDefinition parameterDefinition = deserializer.getDeserializedFoundationActionParameterDefinition(node);

            FoundationActionParameterValue toReturn = new FoundationActionParameterValue<>(parameterDefinition);

            if (toReturn.getJavaType() != null) {
                Class type = toReturn.getJavaType();
                //Class type = Class.forName(typeString);
                toReturn.setJavaType(type);
                if (node.has("value")) {
                    if (type.isAssignableFrom(String.class)) {
                        toReturn.setValue(node.get("value").asText());
                    } else if (type.isAssignableFrom(Date.class)) {
                        toReturn.setValue(ctxt.parseDate(node.get("value").asText()));
                    } else {
                        Object value = mapper.readValue(node.get("value").toString(), type);
                        toReturn.setValue(value);
                    }
                }

            }
            return toReturn;
    }

    @Override
    public Class<?> handledType() {
        return FoundationActionParameterValue.class;
    }

}
