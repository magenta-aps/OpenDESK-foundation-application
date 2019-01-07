/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import junit.framework.Assert;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;

/**
 *
 * @author martin
 */
public class AbstractTestClass extends BaseWebScriptTest {
    public static final String DELIMITER = "/";
    
    private final String basePath;
    

    public AbstractTestClass(String path) {
        this.basePath = path;
    }
    
    protected <R> R get(Class<R> returnType) throws IOException{
        return get(returnType, null);
    }
    
    protected <R> R get(Class<R> returnType, String path) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        TestWebScriptServer.GetRequest request = new TestWebScriptServer.GetRequest(getPath(path));
        request.setHeaders(Collections.singletonMap("Accept", "application/json"));
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK, TestUtils.ADMIN_USER);
        return mapper.readValue(response.getContentAsString(), returnType);
    }
    
    protected <R, C extends Collection<R>> C get(Class<C> collectionType, Class<R> returnType) throws IOException{
        return get(collectionType, returnType, null);
    }
    
    protected <R, C extends Collection<R>> C get(Class<C> collectionType, Class<R> returnType, String path) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        CollectionType type = mapper.getTypeFactory().constructCollectionType(collectionType, returnType);
        TestWebScriptServer.GetRequest request = new TestWebScriptServer.GetRequest(getPath(path));
        request.setHeaders(Collections.singletonMap("Accept", "application/json"));
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK, TestUtils.ADMIN_USER);
        return mapper.readValue(response.getContentAsString(), type);
    }
    
    protected <S, R, C extends Collection<R>> C post(S toSend, Class<C> collection, Class<R> recieve) throws IOException{
        return post(toSend, collection, recieve, null);
    }
    
    protected <S, R, C extends Collection<R>> C post(S toSend, Class<C> collection, Class<R> recieve, String path) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        CollectionType type = mapper.getTypeFactory().constructCollectionType(collection, recieve);
        String data = getContent(toSend, mapper);

        TestWebScriptServer.Request request = new TestWebScriptServer.PostRequest(getPath(path), data, "application/json");
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK, TestUtils.ADMIN_USER);
        return mapper.readValue(response.getContentAsString(), type);
        
    }
    
    protected <S> void post(S toSend) throws IOException, JSONException {
        String noPath = null;
        post(toSend, noPath);
    }
    
    protected <S> void post(S toSend, String path) throws IOException, JSONException {
        post(toSend, null, path);
    }
    
    protected <S, R> R post(S toSend, Class<R> recieve) throws IOException {
        return post(toSend, recieve, null);
    }
    
    protected <S, R> R post(S toSend, Class<R> recieve, String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String data = getContent(toSend, mapper);

        TestWebScriptServer.Request request = new TestWebScriptServer.PostRequest(getPath(path), data, "application/json");
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK, TestUtils.ADMIN_USER);
        if(recieve != null){
            return mapper.readValue(response.getContentAsString(), recieve);
        }
        return null;
        
    }
    
    protected <S> String getContent(S toSend, ObjectMapper mapper) throws IOException{
        String data;
        Objects.requireNonNull(toSend);
        if (toSend instanceof JSONObject) {
            data = toSend.toString();
        } else if (toSend instanceof JSONArray) {
            data = toSend.toString();
        }else{
            
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, toSend);
            data = writer.toString();
        }
        
        return data;
    }

    public <T> void containsSameElements(Collection<? extends T> c1, Collection<? extends T> c2) {
        c1 = new ArrayList<>(c1);
        c2 = new ArrayList<>(c2);
        Assert.assertEquals(c1.size(), c2.size());
        while (c1.size() > 0) {
            T object = c1.iterator().next();
            c1.remove(object);
            c2.remove(object);
            Assert.assertEquals("Collections did not contain the same elements. Could not find "+object, c1.size(), c2.size());
        }

        Assert.assertEquals("Collections did not contain the same elements", 0, c1.size() + c2.size());

    }
    
    protected String getPath(String pathSuffix){
        String toReturn;
        if(basePath.endsWith("/")){
            toReturn = basePath.substring(0, basePath.length());
        }else{
            toReturn = basePath;
        }
        if(pathSuffix != null && !pathSuffix.isEmpty()){
            int beginIndex = pathSuffix.startsWith("/") ? 1 : 0;
            int endIndex = pathSuffix.endsWith("/") ? pathSuffix.length()-1 : pathSuffix.length();
            toReturn = toReturn + DELIMITER + pathSuffix.substring(beginIndex, endIndex);
        }
        return toReturn;
    }

}
