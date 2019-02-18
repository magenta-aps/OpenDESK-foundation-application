package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationChange;
import dk.opendesk.foundationapplication.DAO.ApplicationChangeUnit;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.repo.beans.NodeBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static dk.opendesk.foundationapplication.Utilities.*;


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
        final boolean PRINT = false;

        NodeRef appRef = TestUtils.application1;
        String origDesc = foundationBean.getApplication(appRef).getShortDescription();

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


        // --- TESTING THE WEBSCRIPT --- //
        List<ApplicationChange> changeLists = get(List.class, ApplicationChange.class, appRef+"/history");

        //Testing the changes made when creating the original version
        ApplicationChange changeList = changeLists.get(3);
        assertEquals("admin", changeList.getModifier());
        assertEquals(serviceRegistry.getPersonService().getPerson("admin").toString(), changeList.getModifierId());
        List<ApplicationChangeUnit> appChanges = changeList.getChanges();
        assertEquals(APPLICATION_CHANGE_CREATED, appChanges.get(0).getChangeType());
        assertEquals(null, appChanges.get(0).getOldValue());

        //Testing the first change
        List<ApplicationChangeUnit> changeUnits = changeLists.get(2).getChanges();
        assertEquals(1, changeUnits.size());
        assertEquals(APPLICATION_PARAM_SHORT_DESCRIPTION, changeUnits.get(0).getChangedField());
        assertEquals(origDesc, changeUnits.get(0).getOldValue());
        assertEquals("First change", changeUnits.get(0).getNewValue());
        assertEquals(APPLICATION_CHANGE_PROP, changeUnits.get(0).getChangeType());

        //Testing the second change
        changeUnits = changeLists.get(1).getChanges();
        assertEquals(1, changeUnits.size());
        assertEquals(STATE_PARAM_TITLE, changeUnits.get(0).getChangedField());
        assertEquals(foundationBean.getState(TestUtils.stateRecievedRef).getTitle(), changeUnits.get(0).getOldValue());
        assertEquals(foundationBean.getState(TestUtils.stateAccessRef).getTitle(), changeUnits.get(0).getNewValue());
        assertEquals(APPLICATION_CHANGE_STATE, changeUnits.get(0).getChangeType());

        //Testing the third change
        changeUnits = changeLists.get(0).getChanges();
        assertEquals(2, changeUnits.size());
        assertEquals(STATE_PARAM_TITLE, changeUnits.get(0).getChangedField());
        assertEquals(foundationBean.getState(TestUtils.stateAccessRef).getTitle(), changeUnits.get(0).getOldValue());
        assertEquals(foundationBean.getState(TestUtils.stateAcceptedRef).getTitle(), changeUnits.get(0).getNewValue());
        assertEquals(APPLICATION_CHANGE_STATE, changeUnits.get(0).getChangeType());
        assertEquals(APPLICATION_PARAM_SHORT_DESCRIPTION, changeUnits.get(1).getChangedField());
        assertEquals("First change", changeUnits.get(1).getOldValue());
        assertEquals("Third change", changeUnits.get(1).getNewValue());
        assertEquals(APPLICATION_CHANGE_PROP, changeUnits.get(1).getChangeType());

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
