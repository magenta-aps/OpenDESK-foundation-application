/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.JSON.ApplicationPropertyDeserializer;
import dk.opendesk.foundationapplication.JSON.ApplicationPropertySerializer;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import junit.framework.TestCase;

/**
 *
 * @author martin
 */
public class PropertySerializationTest extends TestCase {
    private ObjectMapper mapper;
    
    @Override
    protected void setUp() throws Exception {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ApplicationPropertyValue.class, new ApplicationPropertySerializer());
        module.addDeserializer(ApplicationPropertyValue.class, new ApplicationPropertyDeserializer());
        mapper.registerModule(module);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //mapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss.SSSX"));
        
        
    }

    @Override
    protected void tearDown() throws Exception {
        mapper = null;
    }
    
    public void testSerializationValue() throws Exception{
//        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss.SSSX");
//        Date now = Date.from(Instant.now());
//        String fmt = format.format(now);
//        Date nownow = format.parse(fmt);
//        Object test = mapper.readValue(fmt, Date.class);
        
        ApplicationPropertiesContainer container = new ApplicationPropertiesContainer();
        container.setId("Container1");
        container.setLabel("my label");
        container.setLayout("my layout");
        ApplicationPropertyValue<Long> val1 = new ApplicationPropertyValue<>(Long.valueOf(10), "hidden:false;", "VAL1!", "Val1", Long.class, "Number");
        ApplicationPropertyValue<String> val2 = new ApplicationPropertyValue<>("val2val", "hidden:true;", "VAL2!", "Val2", String.class, "Text");
        ApplicationPropertyValue<Date> val3 = new ApplicationPropertyValue<>(Date.from(Instant.now()), "length:200;", "VAL3!", "Val3", Date.class, "datepicker");
        ApplicationPropertyValue<Reference> val4 = new ApplicationPropertyValue<>(Reference.fromID("1234"), "", "VAL4!", "Val4", Reference.class, "No idea");
        
        container.getProperties().addAll(Arrays.asList(new ApplicationPropertyValue[]{val1, val2, val3, val4}));
        
        System.out.println(container);
        String serialized = mapper.writeValueAsString(container);
        System.out.println("--------------------------------------------------");
        System.out.println(serialized);
        System.out.println("--------------------------------------------------");
        ApplicationPropertiesContainer containerDeserialized = mapper.readValue(serialized, ApplicationPropertiesContainer.class);
        
        System.out.println(containerDeserialized);
        
        
    }
    
}
