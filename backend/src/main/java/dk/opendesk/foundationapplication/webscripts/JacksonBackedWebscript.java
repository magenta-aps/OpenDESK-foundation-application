/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.Utilities;
import static dk.opendesk.foundationapplication.Utilities.stringExists;
import dk.opendesk.foundationapplication.beans.ActionBean;
import dk.opendesk.foundationapplication.beans.ApplicationBean;
import dk.opendesk.foundationapplication.beans.BranchBean;
import dk.opendesk.foundationapplication.beans.BudgetBean;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.beans.WorkflowBean;
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
import org.alfresco.service.ServiceRegistry;
import org.apache.log4j.MDC;

/**
 *
 * @author martin
 */
public abstract class JacksonBackedWebscript extends AbstractWebScript {

    public static final String MDC_USERID = "webservice.userid";
    public static final String MDC_TICKETID = "webservice.ticketid";
    public static final String MDC_SERVICE_URL = "webservice.url";
    public static final String MDC_MIMETYPE = "webservice.mime";
    public static final String MDC_FORMAT = "webservice.format";

    protected Logger logger = Logger.getLogger(getClass());
    private ObjectMapper mapper;
    private WebScriptRequest req;
    private WebScriptResponse res;
    private Map<String, String> urlParams;
    private Map<String, String> urlQueryParams;
    
    private final Histogram responseTimes = Utilities.getMetrics().histogram(MetricRegistry.name(getClass(), "response-times"));

    private ServiceRegistry serviceRegistry;
    private ActionBean actionBean;
    private ApplicationBean applicationBean;
    private BranchBean branchBean;
    private BudgetBean budgetBean;
    private WorkflowBean workflowBean;

    protected ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public ActionBean getActionBean() {
        return actionBean;
    }

    public void setActionBean(ActionBean actionBean) {
        this.actionBean = actionBean;
    }

    public ApplicationBean getApplicationBean() {
        return applicationBean;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public BranchBean getBranchBean() {
        return branchBean;
    }

    public void setBranchBean(BranchBean branchBean) {
        this.branchBean = branchBean;
    }

    public BudgetBean getBudgetBean() {
        return budgetBean;
    }

    public void setBudgetBean(BudgetBean budgetBean) {
        this.budgetBean = budgetBean;
    }

    public WorkflowBean getWorkflowBean() {
        return workflowBean;
    }

    public void setWorkflowBean(WorkflowBean workflowBean) {
        this.workflowBean = workflowBean;
    }
    
    

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        if (req.getFormat() != null) {
            MDC.put(MDC_FORMAT, req.getFormat());
        }
        if (req.getContentType() != null) {
            MDC.put(MDC_MIMETYPE, req.getContentType());
        }
        MDC.put(MDC_USERID, serviceRegistry.getAuthenticationService().getCurrentUserName());
        MDC.put(MDC_TICKETID, serviceRegistry.getAuthenticationService().getCurrentTicket());
        MDC.put(MDC_SERVICE_URL, req.getURL());
        logger.debug("Service called");
        mapper = Utilities.getMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.req = req;
        this.res = res;
        urlParams = req.getServiceMatch().getTemplateVars();
        urlQueryParams = parseUrlParams(req.getURL());
        try {
            Long startTime = System.currentTimeMillis();
            Object returnData = doAction(req, res);
            responseTimes.update(System.currentTimeMillis()-startTime);
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
            MDC.remove(MDC_FORMAT);
            MDC.remove(MDC_MIMETYPE);
            MDC.remove(MDC_USERID);
            MDC.remove(MDC_TICKETID);
            MDC.remove(MDC_SERVICE_URL);
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

    protected <T> T getRequestListAs(Class<T> clazz) throws IOException {
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

    protected void resolveNodeRef(Reference reference, String nodeID) {
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
