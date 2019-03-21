/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.JSON;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.Utilities;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author martin
 */
public class ApplicationPropertyDeserializer extends JsonDeserializer<ApplicationFieldValue> {

    private static final Logger logger = Logger.getLogger(ApplicationPropertyDeserializer.class);

    @Override
    public ApplicationFieldValue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper mapper = Utilities.getMapper();
        try {
            ApplicationFieldValue toReturn = new ApplicationFieldValue();
            JsonNode node = jp.getCodec().readTree(jp);
            if (node.has("id")) {
                toReturn.setId(node.get("id").asText());
            }
            if (node.has("component")) {
                toReturn.setComponent(node.get("component").asText());
            }
            if (node.has("label")) {
                toReturn.setLabel(node.get("label").asText());
            }
            if (node.has("layout")) {
                toReturn.setLayout(node.get("layout").asText());
            }
            if (node.has("describes")) {
                toReturn.setDescribes(node.get("describes").asText());
            }
            if (node.has("type")) {
                String typeString = node.get("type").asText();
                Class type = Class.forName(typeString);
                toReturn.setType(type);
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

        } catch (ClassNotFoundException ex) {
            logger.error("Failed to deserialize ApplicationFieldValue", ex);
            return null;
        }
    }

    @Override
    public Class<?> handledType() {
        return ApplicationFieldValue.class;
    }

}
