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
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import dk.opendesk.foundationapplication.Utilities;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author martin
 */
public class ApplicationPropertyDeserializer extends JsonDeserializer<ApplicationPropertyValue> {

    @Override
    public ApplicationPropertyValue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper mapper = Utilities.getMapper();
        try {
            ApplicationPropertyValue toReturn = new ApplicationPropertyValue();
            JsonNode node = jp.getCodec().readTree(jp);
            if (node.has("id")) {
                toReturn.setId(node.get("id").asText());
            }
            if (node.has("type")) {
                toReturn.setType(node.get("type").asText());
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
            if (node.has("javaType")) {
                String typeString = node.get("javaType").asText();
                Class type = Class.forName(typeString);
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

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ApplicationPropertyDeserializer.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Class<?> handledType() {
        return ApplicationPropertyValue.class;
    }

}
