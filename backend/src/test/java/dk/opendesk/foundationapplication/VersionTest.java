package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationChange;
import dk.opendesk.foundationapplication.DAO.ApplicationChangeList;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.repo.beans.AuthorityBean;
import dk.opendesk.repo.beans.NodeBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.tests.client.data.Person;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public class VersionTest extends AbstractTestClass {
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");
    private final NodeBean nodeBean = (NodeBean) getServer().getApplicationContext().getBean("nodeBean");

    VersionService versionService = serviceRegistry.getVersionService();

    public VersionTest() {
        super("foundation/application");
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

        NodeRef appRef = TestUtils.application1;

        assertEquals(1,versionService.getVersionHistory(appRef).getAllVersions().size());

        if (PRINT) printHistory(appRef);


        // --- FIRST CHANGE --- //
        if (PRINT) System.out.println("Change #1: Changing the 'description' property\n");

        Application change1 = new Application();
        change1.parseRef(appRef);
        change1.setShortDescription("First change");
        foundationBean.updateApplication(change1);

        //There should now be two versions in the history
        Application headVersion = foundationBean.getApplication(versionService.getVersionHistory(appRef).getHeadVersion().getFrozenStateNodeRef());
        assertEquals(2, versionService.getVersionHistory(appRef).getAllVersions().size());
        assertEquals("First change", headVersion.getShortDescription());

        if (PRINT) printHistory(appRef);


        // --- SECOND CHANGE --- //
        if (PRINT) System.out.println("Change #2: Changing the state to 'assess'\n");

        Application change2 = new Application();
        change2.parseRef(appRef);
        StateReference stateAssess = new StateReference();
        stateAssess.parseRef(TestUtils.stateAccessRef);
        change2.setState(stateAssess);
        foundationBean.updateApplication(change2);

        //There should now be two versions in the history and the newest on should be on state 'assess' and with desc = 'First change'
        headVersion = foundationBean.getApplication(versionService.getVersionHistory(appRef).getHeadVersion().getFrozenStateNodeRef());
        assertEquals(3, versionService.getVersionHistory(appRef).getAllVersions().size());
        assertEquals(TestUtils.stateAccessRef, headVersion.getState().asNodeRef());
        assertEquals("First change", headVersion.getShortDescription());

        if (PRINT) printHistory(appRef);


        // --- THIRD CHANGE --- //
        if (PRINT) System.out.println("Change #3: Changing both state and description\n");

        Application change3 = new Application();
        change3.parseRef(appRef);
        StateReference stateAccepted = new StateReference();
        stateAccepted.parseRef(TestUtils.stateAcceptedRef);
        change3.setState(stateAccepted);
        change3.setShortDescription("Third change");
        foundationBean.updateApplication(change3);

        //There should now be three versions and the newest one should be on state 'accepted' and have description = 'Third change'
        headVersion = foundationBean.getApplication(versionService.getVersionHistory(appRef).getHeadVersion().getFrozenStateNodeRef());
        assertEquals(4, versionService.getVersionHistory(appRef).getAllVersions().size());
        assertEquals(TestUtils.stateAcceptedRef, headVersion.getState().asNodeRef());
        assertEquals("Third change", headVersion.getShortDescription());

        //Current version should be on state 'accepted' and have description = 'Third change'
        Application currentVersion = foundationBean.getApplication(TestUtils.application1);
        assertEquals(TestUtils.stateAcceptedRef, currentVersion.getState().asNodeRef());
        assertEquals("Third change", currentVersion.getShortDescription());

        if (PRINT) printHistory(appRef);


        Set<NodeRef> people = serviceRegistry.getPersonService().getAllPeople();
        for (NodeRef pRef : people) {
            String userName = serviceRegistry.getPersonService().getPerson(pRef).getUserName();
            System.out.println(userName);
        }



        List<ApplicationChangeList> changes = foundationBean.getApplicationHistory(appRef);

        System.out.println(changes);

        for (ApplicationChangeList changeList : changes) {
            System.out.println("--");
            System.out.println("Time: " + changeList.getTimesStamp());
            System.out.println("Modifier: " + serviceRegistry.getPersonService().getPerson(changeList.getModifier()).getUserName());
            for (ApplicationChange change : changeList.getChanges()) {
                System.out.println("change: ");
                System.out.println("\tfield: " + change.getChangedField());
                System.out.println("\told value: " + change.getOldValue());
                System.out.println("\tnew value: " + change.getNewValue());
                System.out.println("\ttyp: " + change.getChangeType());
            }
        }

        System.out.println("====================");
        System.out.println(get(List.class, ApplicationChangeList.class, appRef+"/history"));


    }

    private void printHistory(NodeRef application) throws Exception {

        System.out.println("---------Printing history-------");

        System.out.println("Current version (also in version history):\n");
        System.out.println("\tNodeRef:                        " + application);
        Application app = foundationBean.getApplication(application);
        if (app.getState() != null) {
            System.out.println("\tState:                          " + app.getState().getTitle());
        }
        System.out.println("\tDescription:                    " + app.getShortDescription());
        System.out.println("\temail:                          " + app.getContactEmail());

        VersionHistory history = serviceRegistry.getVersionService().getVersionHistory(application);
        if (history != null) {
            Collection<Version> versions = history.getAllVersions();
            System.out.println("\n\n\nVersions in version history (" + versions.size() + "):");
            for (Version ver : versions) {
                System.out.println("\nVersion:");
                System.out.println("\tnode-uuid (Props):                                           " + ver.getVersionProperties().get("node-uuid"));
                System.out.println("\tFrozenStateNodeRef (getMethod): " + ver.getFrozenStateNodeRef());
                System.out.println();
                System.out.println("\tFrozenStateNodeRef (Props):     " + ver.getVersionProperties().get("frozenNodeRef"));
                System.out.println("\tVersionNodeRef (getMethod):     " + ver.getVersionedNodeRef());
                System.out.println("\tname (Props):                                           " + ver.getVersionProperties().get("name"));
                System.out.println();

                app = foundationBean.getApplication(ver.getFrozenStateNodeRef());
                if (app.getState() != null) {
                    System.out.println("\tState:                          " + app.getState().getTitle());
                }
                System.out.println("\tDescription:                    " + app.getShortDescription());
                System.out.println("\temail:                          " + app.getContactEmail());

                //uncomment to print all properties on a version:
                /*
                System.out.println();
                for (String s : ver.getVersionProperties().keySet()) {
                    System.out.println("\t" + s + ": " + ver.getVersionProperties().get(s));
                    //System.out.println(ver);
                }
                */

            }
        }
        System.out.println("--------------------------------\n\n\n\n");
    }

}
