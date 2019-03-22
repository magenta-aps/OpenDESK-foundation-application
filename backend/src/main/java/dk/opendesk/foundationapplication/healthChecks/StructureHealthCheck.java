package dk.opendesk.foundationapplication.healthChecks;

import com.codahale.metrics.health.HealthCheck;
import dk.opendesk.foundationapplication.Utilities;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;


public class StructureHealthCheck extends HealthCheck {

    ServiceRegistry serviceRegistry;

    public StructureHealthCheck(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public HealthCheck.Result check() throws Exception {

        //admin user exists
        if (serviceRegistry.getPersonService().getPersonOrNull("admin") == null) {
            return HealthCheck.Result.unhealthy("Cannot find admin user");
        }

        //dataHome exists
        try {
            Utilities.getDataNode(serviceRegistry);
        } catch (AlfrescoRuntimeException e) {
            return HealthCheck.Result.unhealthy("Cannot find dataHome");
        }

        //emailTemplateFolder created
        try {
            Utilities.getOdfEmailTemplateFolder(serviceRegistry);
        } catch (AlfrescoRuntimeException e) {
            return HealthCheck.Result.unhealthy("Cannot find emailTemplateFolder");
        }

        return HealthCheck.Result.healthy();
    }
}
