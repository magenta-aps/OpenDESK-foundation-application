/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.JSON;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import java.io.IOException;

/**
 *
 * @author martin
 */
public class ApplicationPropertySerializer extends JsonSerializer<ApplicationPropertyValue>{

    @Override
    public void serialize(ApplicationPropertyValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        if(value.getId() != null){
            jgen.writeStringField("id", value.getId());
        }
        if(value.getType() != null){
            jgen.writeStringField("type", value.getType());
        }
        if(value.getLabel() != null){
            jgen.writeStringField("label", value.getLabel());
        }
        if(value.getJavaType() != null){
            jgen.writeStringField("javaType", value.getJavaType().getCanonicalName());
        }
        if(value.getValue() != null){
            jgen.writeObjectField("value", value.getValue());
        }
        if(value.getLayout() != null){
            jgen.writeObjectField("layout", value.getLayout());
        }
        if(value.getDescribes() != null){
            jgen.writeObjectField("describes", value.getDescribes());
        }
        jgen.writeEndObject();
    }

    @Override
    public Class<ApplicationPropertyValue> handledType() {
        return ApplicationPropertyValue.class;
    }
    
    
    
}
