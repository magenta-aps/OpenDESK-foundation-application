package dk.opendesk.foundationapplication.beans;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import dk.opendesk.foundationapplication.healthChecks.StructureHealthCheck;
import org.alfresco.service.ServiceRegistry;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static dk.opendesk.foundationapplication.Utilities.HEALTH_CHECK_STRUCTURE;


public class HealthCheckBean{

    private ServiceRegistry serviceRegistry;

    private FoundationBean foundationBean;
    private ActionBean actionBean;
    private ApplicationBean applicationBean;
    private BranchBean branchBean;
    private BudgetBean budgetBean;
    private WorkflowBean workflowBean;
    final HealthCheckRegistry healthChecks = new HealthCheckRegistry();

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    public void setActionBean(ActionBean actionBean) {
        this.actionBean = actionBean;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setBranchBean(BranchBean branchBean) {
        this.branchBean = branchBean;
    }

    public void setBudgetBean(BudgetBean budgetBean) {
        this.budgetBean = budgetBean;
    }

    public void setWorkflowBean(WorkflowBean workflowBean) {
        this.workflowBean = workflowBean;
    }



    public void init() {
        healthChecks.register(HEALTH_CHECK_STRUCTURE, new StructureHealthCheck(serviceRegistry));
    }

    public JSONObject runCheck() throws JSONException {
        Map<String, HealthCheck.Result> results= healthChecks.runHealthChecks();
        JSONObject jsonResult = new JSONObject();

        for (String name : results.keySet()) {
            HealthCheck.Result result = results.get(name);
            if (result.isHealthy()) {
                jsonResult.put(name, new JSONObject().put("healthy", true));
            } else {
                jsonResult.put(name, new JSONObject().put("healthy", false).put("errorMsg", result.getMessage()));
            }
        }

        return jsonResult;
    }
}
