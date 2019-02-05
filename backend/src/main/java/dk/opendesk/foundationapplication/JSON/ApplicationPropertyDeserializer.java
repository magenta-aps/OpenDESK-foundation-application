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
import com.fasterxml.jackson.databind.SerializationFeature;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author martin
 */
public class ApplicationPropertyDeserializer extends JsonDeserializer<ApplicationPropertyValue>{


    @Override
    public ApplicationPropertyValue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper newMapper = new ObjectMapper();
        try {
            ApplicationPropertyValue toReturn = new ApplicationPropertyValue();
            JsonNode node = jp.getCodec().readTree(jp);
            toReturn.setId(node.get("id").asText());
            toReturn.setType(node.get("type").asText());
            toReturn.setLabel(node.get("label").asText());
            toReturn.setLayout(node.get("layout").asText());
            String typeString = node.get("javaType").asText();
            Class type = Class.forName(typeString);
            toReturn.setJavaType(type);
            //String valueString = node.get("value").toString();
            if(type.isAssignableFrom(String.class)){
                toReturn.setValue(node.get("value").asText());
            }else if(type.isAssignableFrom(Date.class)){
                toReturn.setValue(ctxt.parseDate(node.get("value").asText()));
            }else{
                Object value = newMapper.readValue(node.get("value").toString(), type);
                toReturn.setValue(value);
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
