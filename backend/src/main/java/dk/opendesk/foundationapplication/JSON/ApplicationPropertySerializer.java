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
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;

import java.io.IOException;

/**
 *
 * @author martin
 */
public class ApplicationPropertySerializer extends JsonSerializer<ApplicationFieldValue>{

    @Override
    public void serialize(ApplicationFieldValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        if(value.getId() != null){
            jgen.writeStringField("id", value.getId());
        }
        if(value.getComponent() != null){
            jgen.writeStringField("component", value.getComponent());
        }
        if(value.getLabel() != null){
            jgen.writeStringField("label", value.getLabel());
        }
        if(value.getValue() != null){
            if(value.isSingleValue()){
                jgen.writeObjectField("value", value.getSingleValue());
            }else{
                jgen.writeObjectField("value", value.getValue());
            }         
        }
        if(value.getLayout() != null){
            jgen.writeObjectField("layout", value.getLayout());
        }
        if(value.getDescribes() != null){
            jgen.writeObjectField("describes", value.getDescribes());
        }
        if(value.getHint() != null){
            jgen.writeStringField("hint", value.getHint());
        }
        if(value.getWrapper() != null){
            jgen.writeStringField("wrapper", value.getWrapper());
        }
        if(value.getValidation() != null){
            jgen.writeStringField("validation", value.getValidation());
        }
        if(value.getReadOnly() != null){
            jgen.writeBooleanField("readOnly", value.getReadOnly());
        }
        if(value.getType() != null){
            jgen.writeStringField("type", value.getType());
        }
        jgen.writeEndObject();
    }

    @Override
    public Class<ApplicationFieldValue> handledType() {
        return ApplicationFieldValue.class;
    }
    
    
    
}
