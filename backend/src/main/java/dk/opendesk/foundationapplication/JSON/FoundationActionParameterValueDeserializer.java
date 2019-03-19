package dk.opendesk.foundationapplication.JSON;

import dk.opendesk.foundationapplication.DAO.FoundationActionParameterValue;
import dk.opendesk.foundationapplication.Utilities;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class FoundationActionParameterValueDeserializer extends JsonDeserializer<FoundationActionParameterValue> {

    ServiceRegistry serviceRegistry;

    @Override
    public FoundationActionParameterValue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper newMapper = new ObjectMapper();
        try {
            FoundationActionParameterValue toReturn = new FoundationActionParameterValue();
            JsonNode node = jp.getCodec().readTree(jp);
            if (node.has("name")) {
                toReturn.setName(node.get("name").asText());
            }
            if (node.has("isMultivalued")) {
                toReturn.setMultivalued(node.get("isMultivalued").asBoolean());
            }
            if (node.has("isMandatory")) {
                toReturn.setMandatory(node.get("isMandatory").asBoolean());
            }
            if (node.has("displayLabel")) {
                toReturn.setDisplayLabel(node.get("displayLabel").asText());
            }
            if (node.has("parameterConstraintName")) {
                toReturn.setParameterConstraintName(node.get("parameterConstraintName").asText());
            }
            if (node.has("type")) {
                toReturn.setType(node.get("type").asText());
                QName qName = QName.createQName("http://www.alfresco.org/model/dictionary/1.0", node.get("type").asText());
                String javaClassName = serviceRegistry.getDictionaryService().getDataType(qName).getJavaClassName();
                Class javaClass = Class.forName(javaClassName);
                if (javaClass.isAssignableFrom(String.class)) {
                    toReturn.setValue(node.get("value").asText());
                }
                //todo parse other types
            }
        } catch (ClassNotFoundException e) {
            //todo Logger.getLogger(getClass()).
        }

        return null;
    }
}
