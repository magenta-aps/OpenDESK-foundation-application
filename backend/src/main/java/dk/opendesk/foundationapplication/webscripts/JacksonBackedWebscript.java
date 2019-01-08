/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.opendesk.foundationapplication.DAO.Reference;
import static dk.opendesk.foundationapplication.Utilities.stringExists;
import static dk.opendesk.foundationapplication.webscripts.foundation.UpdateBudget.BUDGET_DID_NOT_MATCH;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import org.alfresco.error.AlfrescoRuntimeException;

/**
 *
 * @author martin
 */
public abstract class JacksonBackedWebscript extends AbstractWebScript {

    private Logger logger = Logger.getLogger(getClass());
    private ObjectMapper mapper;
    private WebScriptRequest req;
    private WebScriptResponse res;
    private Map<String, String> urlParams;
    private Map<String, String> urlQueryParams;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.req = req;
        this.res = res;
        urlParams = req.getServiceMatch().getTemplateVars();
        urlQueryParams = parseUrlParams(req.getURL());
        try {
            Object returnData = doAction(req, res);
            if (returnData != null) {
                if (returnData instanceof JSONObject) {
                    ((JSONObject) returnData).write(res.getWriter());
                } else if (returnData instanceof JSONArray) {
                    ((JSONArray) returnData).writeJSONString(res.getWriter());
                } else {
                    mapper.writerWithDefaultPrettyPrinter().writeValue(res.getWriter(), returnData);
                }
            }
        } catch (Exception e) {
            error(res, e);
        } finally {
            this.req = null;
            this.res = null;
            urlParams = null;
            urlQueryParams = null;
        }
    }

    protected abstract Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception;

    protected <T> T getRequestAs(Class<T> clazz) throws IOException, JSONException {
        if (clazz.isAssignableFrom(JSONObject.class)) {
            return (T) new JSONObject(req.getContent().getContent());
        } else {
            return mapper.readValue(req.getContent().getContent(), clazz);
        }
    }
    
    protected <T> T getRequestListAs(Class<T> clazz) throws IOException{
        return mapper.readValue(req.getContent().getContent(), mapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public Map<String, String> getUrlParams() {
        return Collections.unmodifiableMap(urlParams);
    }

    public Map<String, String> getUrlQueryParams() {
        return Collections.unmodifiableMap(urlQueryParams);
    }
    
    private void error(WebScriptResponse res, Exception e) throws IOException {
        logger.error("Encountered error when executing script", e);
        try {
            JSONObject obj = new JSONObject().put("error", e.getStackTrace()[0].toString());
            obj.write(res.getWriter());
        } catch (JSONException jsonE) {
            throw new IOException("Could not parse error to JSON: " + jsonE.getMessage(), e); //I am swallowing the json exception and reporting the exception that caused the error. Consider using a compoundexception instead.
        } finally {
            res.setStatus(400);
        }

    }

    private Map<String, String> parseUrlParams(String url) {
        int queryStringStart = url.indexOf('?');
        String queryString = "";
        if (queryStringStart != -1) {
            queryString = url.substring(queryStringStart + 1);
        }
        Map<String, String> parameters = URLEncodedUtils
                .parse(queryString, Charset.forName("UTF-8"))
                .stream()
                .collect(
                        Collectors.groupingBy(
                                NameValuePair::getName,
                                Collectors.collectingAndThen(Collectors.toList(), this::paramValuesToString)));
        return parameters;
    }

    private String paramValuesToString(List<NameValuePair> paramValues) {
        if (paramValues.size() == 1) {
            return paramValues.get(0).getValue();
        }
        List<String> values = paramValues.stream().map(NameValuePair::getValue).collect(Collectors.toList());
        return "[" + StringUtils.join(values, ",") + "]";
    }
    
    protected void resolveNodeRef(Reference reference, String nodeID){
        if (stringExists(reference.getNodeID()) && !reference.getNodeID().equals(nodeID)) {
            throw new AlfrescoRuntimeException(BUDGET_DID_NOT_MATCH);
        }

        if (!stringExists(reference.getNodeID())) {
            if (!stringExists(reference.getStoreID())) {
                reference.fromNodeID(nodeID);
            } else {
                reference.setNodeID(nodeID);
            }
        }
    }

}
