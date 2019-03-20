package dk.opendesk.foundationapplication.JSON;

import dk.opendesk.foundationapplication.DAO.FoundationActionParameterDefinition;
import org.alfresco.service.namespace.QName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class FoundationActionParameterDefinitionDeserializer extends JsonDeserializer<FoundationActionParameterDefinition> {


    @Override
    public FoundationActionParameterDefinition deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        return getDeserializedFoundationActionParameterDefinition(node);
    }

    public FoundationActionParameterDefinition getDeserializedFoundationActionParameterDefinition(JsonNode node) {
        try {
            FoundationActionParameterDefinition toReturn = new FoundationActionParameterDefinition<>();
            if (node.has("name")) {
                toReturn.setName(node.get("name").asText());
            }
            if (node.has("isMultivalued")) {
                toReturn.setMultiValued(node.get("isMultivalued").asBoolean());
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
                toReturn.setType(QName.createQName("http://www.alfresco.org/model/dictionary/1.0", node.get("type").asText()));
            }
            if (node.has("javaType")) {
                Class javaClass = Class.forName(node.get("javaType").asText());
                toReturn.setJavaType(javaClass);
            }

            return toReturn;
        } catch (ClassNotFoundException e) {
            //todo Logger.getLogger(getClass()).
            return null;
        }
    }

    @Override
    public Class<?> handledType() {
        return FoundationActionParameterDefinition.class;
    }

}
