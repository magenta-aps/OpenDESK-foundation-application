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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            if (node.has("hint")) {
                toReturn.setHint(node.get("hint").asText());
            }
            if (node.has("wrapper")) {
                toReturn.setWrapper(node.get("wrapper").asText());
            }
            if (node.has("validation")) {
                toReturn.setValidation(node.get("validation").asText());
            }
            if (node.has("readOnly")) {
                toReturn.setReadOnly(node.get("readOnly").asBoolean());
            }
            if (node.has("type")) {
                String typeString = node.get("type").asText();
                Class type = Class.forName(typeString);
                toReturn.setType(type.getCanonicalName());
                if (node.has("value")) {
                    JsonNode valueNode = node.get("value");
                    if (type.isAssignableFrom(String.class)) {
                        if(valueNode.isArray()){
                            ArrayList<String> values = new ArrayList();
                            int i = 0;
                            while(i<valueNode.size()){
                                values.add(valueNode.get(i).asText());
                                i++;
                            }
                            toReturn.setValue(values);
                        }else{
                            toReturn.setSingleValue(valueNode.asText());
                        }
                        
                    } else if (type.isAssignableFrom(Date.class)) {
                        if(valueNode.isArray()){
                            ArrayList<Date> values = new ArrayList();
                            int i = 0;
                            while(i<valueNode.size()){                            
                                values.add(ctxt.parseDate(valueNode.get(i).asText()));
                                i++;
                            }
                            toReturn.setValue(values);
                        }else{
                            toReturn.setSingleValue(ctxt.parseDate(valueNode.asText()));
                        }
                    } else {
                        if(valueNode.isArray()){
                            ArrayList<Object> values = new ArrayList();
                            int i = 0;
                            while(i<valueNode.size()){                            
                                values.add(mapper.readValue(valueNode.get(i).asText(), type));
                                i++;
                            }
                            toReturn.setValue(values);
                        }else{
                            toReturn.setSingleValue(mapper.readValue(valueNode.toString(), type));
                        }
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
