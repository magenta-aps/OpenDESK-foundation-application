package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.repo.beans.EmailBean;
import dk.opendesk.repo.beans.NodeBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VersionTest extends AbstractTestClass {
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");
    private final EmailBean emailBean = (EmailBean) getServer().getApplicationContext().getBean("emailBean");
    //private final NodeBean nodeBean = (NodeBean) getServer().getApplicationContext().getBean("nodeBean");

    public VersionTest() {
        super("");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(serviceRegistry);
        TestUtils.setupSimpleFlow(serviceRegistry);
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.wipeData(serviceRegistry);
    }

    public void testVersioning() throws Exception {
        NodeRef appRef = TestUtils.application1;
        Application change = new Application();
        change.parseRef(appRef);
        StateReference ref = new StateReference();
        ref.parseRef(TestUtils.stateAccessRef);
        change.setState(ref);
        foundationBean.updateApplication(change);

        VersionHistory history = serviceRegistry.getVersionService().getVersionHistory(TestUtils.application1);
        Collection<Version> versions = history.getAllVersions();
        for (Version ver : versions) {
            System.out.println("Version:");
            for (String s : ver.getVersionProperties().keySet()) {
                System.out.println("\t" + s + ": " + ver.getVersionProperties().get(s));
                //System.out.println(ver);
            }
        }

        System.out.println("---");

        //NodeRef appRef2 = TestUtils.application1;
        Application change2 = new Application();
        change2.parseRef(appRef);
        //StateReference ref2 = new StateReference();
        //ref2.parseRef(TestUtils.stateDeniedRef);
        //change2.setState(ref2);
        //foundationBean.updateApplication(change2);



        change2.setShortDescription("TEST");
        change2.setContactEmail("test@test.dk");

        //change2.setContactEmail("test@versioning.dk");
        foundationBean.updateApplication(change2);

        history = serviceRegistry.getVersionService().getVersionHistory(TestUtils.application1);
        versions = history.getAllVersions();
        for (Version ver : versions) {
            System.out.println("Version:");
            for (String s : ver.getVersionProperties().keySet()) {
                System.out.println("\t" + s + ": " + ver.getVersionProperties().get(s));
                //System.out.println(ver);
            }
        }

        System.out.println("------------------------------------------------------------");

        //using method from OpenDesk:

        //JSONArray jsonVersions = nodeBean.getVersions(TestUtils.application1);
        System.out.println();
        //System.out.println(jsonVersions);
        //for (Object j : jsonVersions) {
        //    System.out.println();
        //}


        System.out.println("------------------------------------------------------------");


        //seeing if I can get associations out:

        Version tester = (Version) versions.toArray()[1];
        List<AssociationRef> sourceAssocs = serviceRegistry.getNodeService().getSourceAssocs(tester.getVersionedNodeRef(), Utilities.getODFName(Utilities.APPLICATION_ASSOC_STATE));
        List<AssociationRef> targetAssocs = serviceRegistry.getNodeService().getTargetAssocs(tester.getVersionedNodeRef(), Utilities.getODFName(Utilities.APPLICATION_ASSOC_STATE));

        System.out.println("sourceAssocs:");
        for (AssociationRef a : sourceAssocs) {
            System.out.println("\tget target:");
            System.out.println(serviceRegistry.getNodeService().getType(a.getTargetRef()));
            System.out.println("\tget source:");
            System.out.println(serviceRegistry.getNodeService().getType(a.getSourceRef()));
        }

        System.out.println("targetAssocs:");
        for (AssociationRef a : targetAssocs) {
            System.out.println("\tget target:");
            System.out.println(serviceRegistry.getNodeService().getType(a.getTargetRef()));
            System.out.println("\tget source:");
            System.out.println(serviceRegistry.getNodeService().getType(a.getSourceRef()));
        }
    }


    public Map<String, Serializable> getVersionDifference(Version old, Version neo) {

        throw new UnsupportedOperationException();
    }
}

