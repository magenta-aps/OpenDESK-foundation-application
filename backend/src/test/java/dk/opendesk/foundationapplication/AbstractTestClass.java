/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import com.fasterxml.jackson.databind.type.MapType;
import dk.opendesk.foundationapplication.beans.ActionBean;
import dk.opendesk.foundationapplication.beans.ApplicationBean;
import dk.opendesk.foundationapplication.beans.AuthorityBean;
import dk.opendesk.foundationapplication.beans.BranchBean;
import dk.opendesk.foundationapplication.beans.BudgetBean;
import dk.opendesk.foundationapplication.beans.WorkflowBean;
import java.util.Map;
import junit.framework.Assert;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.ServiceRegistry;
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
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final ActionBean actionBean = (ActionBean) getServer().getApplicationContext().getBean("actionBean");
    private final AuthorityBean authorityBean = (AuthorityBean) getServer().getApplicationContext().getBean("odfAuthBean");
    private final ApplicationBean applicationBean = (ApplicationBean) getServer().getApplicationContext().getBean("applicationBean");
    private final BranchBean branchBean = (BranchBean) getServer().getApplicationContext().getBean("branchBean");
    private final BudgetBean budgetBean = (BudgetBean) getServer().getApplicationContext().getBean("budgetBean");
    private final WorkflowBean workflowBean = (WorkflowBean) getServer().getApplicationContext().getBean("workflowBean");
    private final BehaviourFilter behaviourFilter = (BehaviourFilter) getServer().getApplicationContext().getBean("policyBehaviourFilter");

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public ActionBean getActionBean() {
        return actionBean;
    }

    public ApplicationBean getApplicationBean() {
        return applicationBean;
    }

    public BranchBean getBranchBean() {
        return branchBean;
    }

    public BudgetBean getBudgetBean() {
        return budgetBean;
    }

    public WorkflowBean getWorkflowBean() {
        return workflowBean;
    }

    public String getBasePath() {
        return basePath;
    }

    public AuthorityBean getAuthorityBean() {
        return authorityBean;
    }

    public BehaviourFilter getBehaviourFilter() {
        return behaviourFilter;
    }

    public static final String DELIMITER = "/";
    
    private final String basePath;
    

    public AbstractTestClass(String path) {
        this.basePath = path;
    }
    
    protected <R> R get(Class<R> returnType) throws IOException{
        return get(returnType, null);
    }
    
    protected <R> R get(Class<R> returnType, String path) throws IOException{
        return get(returnType, path, TestUtils.ADMIN_USER);
    }
    
    protected <R> R get(Class<R> returnType, String path, String username) throws IOException{
        ObjectMapper mapper = Utilities.getMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        TestWebScriptServer.GetRequest request = new TestWebScriptServer.GetRequest(getPath(path));
        request.setHeaders(Collections.singletonMap("Accept", "application/json"));
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK, username);
        return mapper.readValue(response.getContentAsString(), returnType);
    }
    
    protected <R, C extends Collection<R>> C get(Class<C> collectionType, Class<R> returnType) throws IOException{
        return get(collectionType, returnType, null);
    }
    
    protected <R, C extends Collection<R>> C get(Class<C> collectionType, Class<R> returnType, String path) throws IOException{
        return get(collectionType, returnType, path, TestUtils.ADMIN_USER);
    }
    protected <R, C extends Collection<R>> C get(Class<C> collectionType, Class<R> returnType, String path, String username) throws IOException{
        ObjectMapper mapper = Utilities.getMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        CollectionType type = mapper.getTypeFactory().constructCollectionType(collectionType, returnType);
        TestWebScriptServer.GetRequest request = new TestWebScriptServer.GetRequest(getPath(path));
        request.setHeaders(Collections.singletonMap("Accept", "application/json"));
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK, username);
        String returnText = response.getContentAsString();
        return mapper.readValue(returnText, type);
    }

    protected <K, V, M extends Map<K, V>> M get(Class<M> mapType, Class<K> keyType, Class<V> valueType, String path) throws IOException{
        return get(mapType, keyType, valueType, path, TestUtils.ADMIN_USER);
    }

    protected <K, V, M extends Map<K, V>> M get(Class<M> mapType, Class<K> keyType, Class<V> valueType, String path, String username) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MapType type = mapper.getTypeFactory().constructMapType(mapType,keyType,valueType);
        TestWebScriptServer.GetRequest request = new TestWebScriptServer.GetRequest(getPath(path));
        request.setHeaders(Collections.singletonMap("Accept", "application/json"));
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK, username);
        return mapper.readValue(response.getContentAsString(), type);
    }


    protected <S, R, C extends Collection<R>> C post(S toSend, Class<C> collection, Class<R> recieve) throws IOException{
        return post(toSend, collection, recieve, null);
    }
    
    protected <S, R, C extends Collection<R>> C post(S toSend, Class<C> collection, Class<R> recieve, String path) throws IOException{
        return post(toSend, collection, recieve, path, TestUtils.ADMIN_USER);
    }    
    
    protected <S, R, C extends Collection<R>> C post(S toSend, Class<C> collection, Class<R> recieve, String path, String username) throws IOException{
        ObjectMapper mapper = Utilities.getMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        CollectionType type = mapper.getTypeFactory().constructCollectionType(collection, recieve);
        String data = getContent(toSend, mapper);

        TestWebScriptServer.Request request = new TestWebScriptServer.PostRequest(getPath(path), data, "application/json");
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK, username);
        return mapper.readValue(response.getContentAsString(), type);
    }
    
    protected <S> void post(S toSend) throws IOException, JSONException {
        String noPath = null;
        post(toSend, noPath);
    }
    
    protected <S> void post(S toSend, String path) throws IOException, JSONException {
        post(toSend, null, path);
    }

    protected <S> void post(S toSend, String path, int statusCode) throws IOException, JSONException {
        post(toSend, null, path, statusCode);
    }

    protected <S, R> R post(S toSend, Class<R> recieve) throws IOException {
        return post(toSend, recieve, null);
    }
    
    protected <S, R> R post(S toSend, Class<R> recieve, String path) throws IOException {
        return post(toSend, recieve, path, Status.STATUS_OK);
    }

    protected <S, R> R post(S toSend, Class<R> recieve, String path, int statusCode) throws IOException {
        return post(toSend, recieve, path, statusCode, TestUtils.ADMIN_USER);

    }
    
        protected <S, R> R post(S toSend, Class<R> recieve, String path, int statusCode, String username) throws IOException {
        ObjectMapper mapper = Utilities.getMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String data = getContent(toSend, mapper);

        TestWebScriptServer.Request request = new TestWebScriptServer.PostRequest(getPath(path), data, "application/json");
        TestWebScriptServer.Response response = sendRequest(request, statusCode, username);
        if(recieve != null){
            return mapper.readValue(response.getContentAsString(), recieve);
        }
        return null;

    }

    protected <R> R delete(Class<R> returnType, String path) throws IOException{
        return delete(returnType, path, TestUtils.ADMIN_USER);
    }
    
        protected <R> R delete(Class<R> returnType, String path, String username) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        TestWebScriptServer.DeleteRequest request = new TestWebScriptServer.DeleteRequest(getPath(path));
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK, username);
        if (returnType == String.class) {
            return (R) response.getContentAsString();
        }
        return mapper.readValue(response.getContentAsString(), returnType);
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
            if(pathSuffix.startsWith("?")){
                toReturn = toReturn + pathSuffix;
            }else{
                int beginIndex = pathSuffix.startsWith("/") ? 1 : 0;
                int endIndex = pathSuffix.endsWith("/") ? pathSuffix.length()-1 : pathSuffix.length();
                toReturn = toReturn + DELIMITER + pathSuffix.substring(beginIndex, endIndex);
            }
            
        }
        return toReturn;
    }

}
