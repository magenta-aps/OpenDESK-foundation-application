package dk.opendesk.foundationapplication;

import com.benfante.jslideshare.App;
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
import org.alfresco.service.cmr.version.VersionService;
import org.json.JSONObject;
import org.json.Test;
import org.json.simple.JSONArray;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static dk.opendesk.foundationapplication.Utilities.ASPECT_ON_CREATE;

public class VersionTest extends AbstractTestClass {
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");
    private final NodeBean nodeBean = (NodeBean) getServer().getApplicationContext().getBean("nodeBean");

    VersionService versionService = serviceRegistry.getVersionService();

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
        final boolean PRINT = true;

        NodeRef appRef = TestUtils.application3;

        //after setup, there should only be one version
        //assertEquals(1,versionService.getVersionHistory(appRef).getAllVersions().size());

        if (PRINT) {
            //printHistory(appRef);
            System.out.println("Change #1: Changing the 'description' property\n");
        }

        Application change1 = new Application();
        change1.parseRef(appRef);
        change1.setShortDescription("First change");
        foundationBean.updateApplication(change1);

        //there should now be two versions
        //assertEquals(2,versionService.getVersionHistory(appRef).getAllVersions().size());

        Application headVersion = foundationBean.getApplication(versionService.getVersionHistory(appRef).getHeadVersion().getFrozenStateNodeRef());
        //assertEquals("First change", headVersion.getShortDescription());

        if (PRINT) printHistory(appRef);


        System.out.println("Setting desc=TEST and email=test@test.dk\n");

        Application change2 = new Application();
        change2.parseRef(appRef);
        change2.setShortDescription("TEST");
        change2.setContactEmail("test@test.dk");
        foundationBean.updateApplication(change2);

        if (PRINT) printHistory(appRef);

        nodeBean.getVersions(TestUtils.application1);

        System.out.println("setting state=assess\n");
        System.out.println("OG HER MANGLER DER VERSIONERING");

        /*
        Application change3 = new Application();
        change3.parseRef(appRef);
        StateReference ref2 = new StateReference();
        ref2.parseRef(TestUtils.stateAccessRef);
        change3.setState(ref2);
        change3.setContactEmail("newEmail@test.dk");
        foundationBean.updateApplication(change3);

        if (PRINT) printHistory(appRef);


        System.out.println("setting desc=and now?");
        Application change4 = new Application();
        change4.parseRef(appRef);
        change4.setShortDescription("and now?");
        foundationBean.updateApplication(change4);

        if (PRINT) printHistory(appRef);


        //sending a mail saved to a state
        JSONObject mailData = new JSONObject();
        mailData.put("stateRef", TestUtils.stateAcceptedRef);
        mailData.put("aspect", ASPECT_ON_CREATE);
        post(mailData,"/foundation/actions/mail");


        Application change5 = new Application();
        change5.parseRef(appRef);
        StateReference ref3 = new StateReference();
        ref3.parseRef(TestUtils.stateAcceptedRef);
        change5.setState(ref3);
        foundationBean.updateApplication(change5);

        if (PRINT) printHistory(appRef);
        */


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

    private void printHistory(NodeRef application) throws Exception {

        System.out.println("---------Printing history-------");

        System.out.println("Current version (not in version history):");
        System.out.println("NodeRef: " + application);
        //Application app =

        //TODO: Print current version

        VersionHistory history = serviceRegistry.getVersionService().getVersionHistory(application);
        Collection<Version> versions = history.getAllVersions();
        System.out.println("# versions in version history = "  + versions.size());
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

