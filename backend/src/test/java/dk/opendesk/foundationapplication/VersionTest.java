package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.State;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.beans.FoundationBean;
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

        printHistory();

        System.out.println("Setting desc='First change'\n");

        NodeRef appRef = TestUtils.application1;
        Application change = new Application();
        change.parseRef(appRef);
        //StateReference ref = new StateReference();

        //ref.parseRef(TestUtils.stateRecievedRef);
        //change.setState(ref);
        change.setShortDescription("First change");
        foundationBean.updateApplication(change);

        printHistory();


        System.out.println("Setting desc=TEST and email=test@test.dk\n");

        Application change2 = new Application();
        change2.parseRef(appRef);
        change2.setShortDescription("TEST");
        change2.setContactEmail("test@test.dk");
        foundationBean.updateApplication(change2);

        printHistory();


        System.out.println("setting state=assess\n");
        System.out.println("OG HER MANGLER DER VERSIONERING");

        Application change3 = new Application();
        change3.parseRef(appRef);
        StateReference ref2 = new StateReference();
        ref2.parseRef(TestUtils.stateAccessRef);
        change3.setState(ref2);
        foundationBean.updateApplication(change3);

        printHistory();


        Application change4 = new Application();
        change4.parseRef(appRef);
        change4.setShortDescription("and now?");
        foundationBean.updateApplication(change4);

        printHistory();
        //System.out.println("------------------------------------------------------------");

        //using method from OpenDesk:

        //JSONArray jsonVersions = nodeBean.getVersions(TestUtils.application1);
        //System.out.println();
        //System.out.println(jsonVersions);
        //for (Object j : jsonVersions) {
        //    System.out.println();
        //}


        //System.out.println("------------------------------------------------------------");


        //seeing if I can get associations out:

        /*
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
        */
    }

    private void printHistory() throws Exception {

        System.out.println("---------Printing history-------");
        VersionHistory history = serviceRegistry.getVersionService().getVersionHistory(TestUtils.application1);
        Collection<Version> versions = history.getAllVersions();
        System.out.println("# versions = "  + versions.size());
        for (Version ver : versions) {
            System.out.println("\nVersion:");
            System.out.println("\tnode-uuid (Props):                                           " + ver.getVersionProperties().get("node-uuid"));
            System.out.println("\tFrozenStateNodeRef (getMethod): " + ver.getFrozenStateNodeRef());
            System.out.println();
            System.out.println("\tFrozenStateNodeRef (Props):     " + ver.getVersionProperties().get("frozenNodeRef"));
            System.out.println("\tVersionNodeRef (getMethod):     " + ver.getVersionedNodeRef());
            System.out.println("\tname (Props):                                           " + ver.getVersionProperties().get("name"));
            System.out.println();

            Application app = foundationBean.getApplication(ver.getFrozenStateNodeRef());
            if (app.getState() != null) {
                System.out.println("\tState:                          " + app.getState().getTitle());
            }
            System.out.println("\tDescription:                    " + app.getShortDescription());
            System.out.println("\temail:                          " + app.getContactEmail());


            //List<AssociationRef> assoc = serviceRegistry.getNodeService().getTargetAssocs(ver.getFrozenStateNodeRef(), Utilities.getODFName(Utilities.APPLICATION_ASSOC_STATE));
            //State state = foundationBean.getState(assoc.get(0).getTargetRef());
            //System.out.println("\tState:                          " + state.getTitle());
            //System.out.println("\tDescription:                    " + ver.getDescription());
            //System.out.println("\temail:                          " + ver.getVersionProperties().get("applicationContactEmail"));

            /*
            System.out.println();
            for (String s : ver.getVersionProperties().keySet()) {
                System.out.println("\t" + s + ": " + ver.getVersionProperties().get(s));
                //System.out.println(ver);
            }
            */

        }
        System.out.println("--------------------------------\n\n\n\n");
    }


    public Map<String, Serializable> getVersionDifference(Version old, Version neo) {

        throw new UnsupportedOperationException();
    }
}

