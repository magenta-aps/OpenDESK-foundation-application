package dk.opendesk.foundationapplication;

import org.alfresco.rest.api.tests.AbstractBaseApiTest;
import org.alfresco.rest.api.tests.client.data.Document;

import java.io.IOException;
import java.util.Map;

public class OdfBaseApiTest extends AbstractBaseApiTest {

    public Document createTextFile(String parentId, String fileName, String textContent) throws IOException, Exception
    {
        return createTextFile(parentId, fileName, textContent, "UTF-8", null);
    }

    public Document createTextFile(String parentId, String fileName, String textContent, String encoding, Map<String, String> props) throws IOException, Exception {
        return createTextFile(parentId, fileName, textContent, encoding, props, 201);
    }

    public Document createTextFile(String parentId, String fileName, String textContent, String encoding, Map<String, String> props, int expectedStatus) throws IOException, Exception {
        return super.createTextFile(parentId,fileName,textContent,encoding,props,expectedStatus);
    }

    @Override
    public String getScope() {
        return "public";
    }

}
