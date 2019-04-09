package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationChange;
import dk.opendesk.foundationapplication.DAO.ApplicationChangeUnit;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.repo.beans.NodeBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static dk.opendesk.foundationapplication.Utilities.*;


public class VersionTest extends AbstractTestClass {
    private final NodeBean nodeBean = (NodeBean) getServer().getApplicationContext().getBean("nodeBean");

    VersionService versionService = getServiceRegistry().getVersionService();

    public VersionTest() {
        super("foundation/application");
    }

    Logger logger = Logger.getLogger(getClass());

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(getServiceRegistry());
        TestUtils.setupSimpleFlow(getServiceRegistry());
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.wipeData(getServiceRegistry());
    }

    public void testVersioningNewApplication() throws Exception {

        Application app = new Application();
        app.setTitle("newApp");
        NodeRef appRef = getApplicationBean().addNewApplication(app).asNodeRef();
        List<ApplicationChange> changeList = get(List.class, ApplicationChange.class, appRef.getId() + "/history");
        System.out.println(changeList);

        Application app2 = new Application();
        app2.setTitle("newApp2");
        NodeRef appRef2 = getApplicationBean().addNewApplication(app2).asNodeRef();
        ApplicationBlock emptyBlock = new ApplicationBlock();
        app2.setBlocks(Arrays.asList(new ApplicationBlock[]{emptyBlock}));
        List<ApplicationChange> changeList2 = get(List.class, ApplicationChange.class, appRef2.getId() + "/history");
        System.out.println(changeList2);

    }

    public void testVersioning() throws Exception {

        NodeRef appRef = TestUtils.application1;
        String origMail = getApplicationBean().getApplication(appRef).emailTo().getSingleValue();
        String origStateTitle = getApplicationBean().getApplication(appRef).getState().getTitle();

        makeChanges(appRef);
        testGetApplicationHistory(appRef, origStateTitle);
        testApplicationHistoryWebScript(appRef, origMail);
    }

    private void makeChanges(NodeRef appRef) throws Exception {
        assertEquals(1, versionService.getVersionHistory(appRef).getAllVersions().size());

        logger.debug("\nChange #0: Application created\n");
        if (logger.isDebugEnabled()) logger.debug(getApplicationBean().getApplicationHistory(appRef));


        // --- FIRST CHANGE --- //
        logger.debug("\nChange #1: Changing the 'email' property\n");

        Application change1 = Utilities.buildChange(getApplicationBean().getApplication(appRef))
                .changeField("8").setValue("First change").done()
                .build();
        getApplicationBean().updateApplication(change1);

        //There should now be two versions in the history
        if (logger.isDebugEnabled()) logger.debug(getApplicationBean().getApplicationHistory(appRef));
        Application headVersion = getApplicationBean().getApplication(versionService.getVersionHistory(appRef).getHeadVersion().getFrozenStateNodeRef());
        assertEquals(2, versionService.getVersionHistory(appRef).getAllVersions().size());
        assertEquals("First change", headVersion.emailTo().getSingleValue());


        // --- SECOND CHANGE --- //
        logger.debug("\nChange #2: Changing the state to 'assess'\n");

        Application change2 = new Application();
        change2.parseRef(appRef);
        StateReference stateAssess = new StateReference();
        stateAssess.parseRef(TestUtils.w1StateAccessRef);
        change2.setState(stateAssess);
        getApplicationBean().updateApplication(change2);

        //There should now be two versions in the history and the newest on should be on state 'assess' and with desc = 'First change'
        headVersion = getApplicationBean().getApplication(versionService.getVersionHistory(appRef).getHeadVersion().getFrozenStateNodeRef());
        assertEquals(3, versionService.getVersionHistory(appRef).getAllVersions().size());
        assertEquals(TestUtils.w1StateAccessRef, headVersion.getState().asNodeRef());
        assertEquals("First change", headVersion.emailTo().getSingleValue());

        if (logger.isDebugEnabled()) logger.debug(getApplicationBean().getApplicationHistory(appRef));


        // --- THIRD CHANGE --- //
        logger.debug("\nChange #3: Changing both state and email\n");

        Application change3 = Utilities.buildChange(getApplicationBean().getApplication(appRef))
                .changeField("8").setValue("Third change").done()
                .build();
        StateReference stateAccepted = new StateReference();
        stateAccepted.parseRef(TestUtils.w1StateAcceptedRef);
        change3.setState(stateAccepted);
        getApplicationBean().updateApplication(change3);

        //There should now be three versions and the newest one should be on state 'accepted' and have email = 'Third change'
        headVersion = getApplicationBean().getApplication(versionService.getVersionHistory(appRef).getHeadVersion().getFrozenStateNodeRef());
        assertEquals(4, versionService.getVersionHistory(appRef).getAllVersions().size());
        assertEquals(TestUtils.w1StateAcceptedRef, headVersion.getState().asNodeRef());
        assertEquals("Third change", headVersion.emailTo().getSingleValue());

        //Current version should be on state 'accepted' and have email = 'Third change'
        Application currentVersion = getApplicationBean().getApplication(appRef);
        assertEquals(TestUtils.w1StateAcceptedRef, currentVersion.getState().asNodeRef());
        assertEquals("Third change", currentVersion.emailTo().getSingleValue());

        if (logger.isDebugEnabled()) logger.debug(getApplicationBean().getApplicationHistory(appRef));


        // --- FOURTH CHANGE --- //
        logger.debug("\nChange #4: Application deleted\n");

        getApplicationBean().deleteApplication(appRef);

        if (logger.isDebugEnabled()) logger.debug(getApplicationBean().getApplicationHistory(appRef));

    }

    private void testGetApplicationHistory(NodeRef appRef, String origStateTitle) throws Exception {

        List<ApplicationChange> appChanges = getApplicationBean().getApplicationHistory(appRef);
        ApplicationChange applicationCreation = appChanges.get(0);
        ApplicationChange applicationUpdate = appChanges.get(3);
        ApplicationChange applicationDeletion = appChanges.get(4);


        //Application creation
        assertEquals(APPLICATION_CHANGE_CREATED, applicationCreation.getChangeType());

        ApplicationChangeUnit stateChange = null;
        ApplicationChangeUnit emailChange = null;
        for (ApplicationChangeUnit unit : applicationCreation.getChanges()) {
            if (unit.getChangedField().equals("State")) {
                stateChange = unit;
            }
            if (unit.getChangedField().equals("Email")) {
                emailChange = unit;
            }
        }
        assertNotNull(stateChange);
        assertEquals(origStateTitle, stateChange.getNewValue());
        assertEquals(APPLICATION_CHANGE_UPDATE_ASSOCIATION, stateChange.getChangeType());

        assertNotNull(emailChange);
        assertEquals("lars@larsen.org", emailChange.getNewValue());
        assertEquals(APPLICATION_CHANGE_UPDATE_PROP, emailChange.getChangeType());

        //Application update state and email
        assertEquals(APPLICATION_CHANGE_UPDATE, applicationUpdate.getChangeType());
        assertEquals(2, applicationUpdate.getChanges().size());
        assertEquals("State", applicationUpdate.getChanges().get(1).getChangedField());
        assertEquals(APPLICATION_CHANGE_UPDATE_ASSOCIATION, applicationUpdate.getChanges().get(1).getChangeType());
        assertEquals("Email", applicationUpdate.getChanges().get(0).getChangedField());
        assertEquals(APPLICATION_CHANGE_UPDATE_PROP, applicationUpdate.getChanges().get(0).getChangeType());

        //Application deletion
        assertEquals(APPLICATION_CHANGE_DELETED, applicationDeletion.getChangeType());
        assertEquals(4, applicationDeletion.getChanges().size());
        assertEquals("Branch", applicationDeletion.getChanges().get(0).getChangedField());
        assertEquals(APPLICATION_CHANGE_UPDATE_ASSOCIATION, applicationDeletion.getChanges().get(0).getChangeType());

    }

    private void testApplicationHistoryWebScript(NodeRef appRef, String origMail) throws Exception {
        List<ApplicationChange> changeList = get(List.class, ApplicationChange.class, appRef.getId() + "/history");

        //Testing the changes made when creating the original version
        ApplicationChange applicationChange = changeList.get(0);
        assertEquals(APPLICATION_CHANGE_CREATED, applicationChange.getChangeType());
        assertEquals("admin", applicationChange.getModifier());
        assertEquals(getServiceRegistry().getPersonService().getPerson("admin").toString(), applicationChange.getModifierId());

        List<ApplicationChangeUnit> appChangesWeb = applicationChange.getChanges();
        ApplicationChangeUnit emailChangeUnit = null;
        ApplicationChangeUnit stateChangeUnit = null;
        for (ApplicationChangeUnit unit : appChangesWeb) {
            if (unit.getChangedField().equals("Email")) {
                emailChangeUnit = unit;
            }
            if (unit.getChangedField().equals("State")) {
                stateChangeUnit = unit;
            }
        }
        assertNotNull(emailChangeUnit);
        assertNotNull(stateChangeUnit);
        assertNull(emailChangeUnit.getOldValue());
        assertNull(stateChangeUnit.getOldValue());
        assertEquals(APPLICATION_CHANGE_UPDATE_PROP, emailChangeUnit.getChangeType());
        assertEquals(APPLICATION_CHANGE_UPDATE_ASSOCIATION, stateChangeUnit.getChangeType());

        //Testing the first change
        applicationChange = changeList.get(1);
        assertEquals(APPLICATION_CHANGE_UPDATE, applicationChange.getChangeType());
        assertEquals("admin", applicationChange.getModifier());
        assertEquals(getServiceRegistry().getPersonService().getPerson("admin").toString(), applicationChange.getModifierId());

        List<ApplicationChangeUnit> changeUnits = applicationChange.getChanges();
        assertEquals(1, changeUnits.size());
        assertEquals("Email", changeUnits.get(0).getChangedField());
        assertEquals(origMail, changeUnits.get(0).getOldValue());
        assertEquals("First change", changeUnits.get(0).getNewValue());
        assertEquals(APPLICATION_CHANGE_UPDATE_PROP, changeUnits.get(0).getChangeType());

        //Testing the second change
        applicationChange = changeList.get(2);
        assertEquals(APPLICATION_CHANGE_UPDATE, applicationChange.getChangeType());
        assertEquals("admin", applicationChange.getModifier());
        assertEquals(getServiceRegistry().getPersonService().getPerson("admin").toString(), applicationChange.getModifierId());

        changeUnits = applicationChange.getChanges();
        assertEquals(1, changeUnits.size());
        assertEquals("State", changeUnits.get(0).getChangedField());
        assertEquals(getWorkflowBean().getState(TestUtils.w1StateRecievedRef).getTitle(), changeUnits.get(0).getOldValue());
        assertEquals(getWorkflowBean().getState(TestUtils.w1StateAccessRef).getTitle(), changeUnits.get(0).getNewValue());
        assertEquals(APPLICATION_CHANGE_UPDATE_ASSOCIATION, changeUnits.get(0).getChangeType());

        //Testing the third change
        applicationChange = changeList.get(3);
        assertEquals(APPLICATION_CHANGE_UPDATE, applicationChange.getChangeType());
        assertEquals("admin", applicationChange.getModifier());
        assertEquals(getServiceRegistry().getPersonService().getPerson("admin").toString(), applicationChange.getModifierId());

        changeUnits = applicationChange.getChanges();
        assertEquals(2, changeUnits.size());
        stateChangeUnit = null;
        emailChangeUnit = null;
        for (ApplicationChangeUnit unit : changeUnits) {
            if (unit.getChangedField().equals("State")) {
                stateChangeUnit = unit;
            }
            if (unit.getChangedField().equals("Email")) {
                emailChangeUnit = unit;
            }
        }
        assertNotNull(stateChangeUnit);
        assertNotNull(emailChangeUnit);

        assertEquals("State", stateChangeUnit.getChangedField());
        assertEquals(getWorkflowBean().getState(TestUtils.w1StateAccessRef).getTitle(), stateChangeUnit.getOldValue());
        assertEquals(getWorkflowBean().getState(TestUtils.w1StateAcceptedRef).getTitle(), stateChangeUnit.getNewValue());
        assertEquals(APPLICATION_CHANGE_UPDATE_ASSOCIATION, stateChangeUnit.getChangeType());
        assertEquals("Email", emailChangeUnit.getChangedField());
        assertEquals("First change", emailChangeUnit.getOldValue());
        assertEquals("Third change", emailChangeUnit.getNewValue());
        assertEquals(APPLICATION_CHANGE_UPDATE_PROP, emailChangeUnit.getChangeType());

        //Testing the application deletion
        applicationChange = changeList.get(4);
        assertEquals(APPLICATION_CHANGE_DELETED, applicationChange.getChangeType());
        assertEquals("admin", applicationChange.getModifier());
        assertEquals(getServiceRegistry().getPersonService().getPerson("admin").toString(), applicationChange.getModifierId());

        changeUnits = applicationChange.getChanges();
        assertEquals(4, changeUnits.size());
        stateChangeUnit = null;
        emailChangeUnit = null;
        for (ApplicationChangeUnit unit : changeUnits) {
            if (unit.getChangedField().equals("State")) {
                stateChangeUnit = unit;
            }
            if (unit.getChangedField().equals("Email")) {
                emailChangeUnit = unit;
            }
        }
        assertNotNull(stateChangeUnit);
        assertNull(emailChangeUnit);
        assertEquals(getWorkflowBean().getState(TestUtils.w1StateAcceptedRef).getTitle(), stateChangeUnit.getOldValue());
        assertNull(stateChangeUnit.getNewValue());
        assertEquals(APPLICATION_CHANGE_UPDATE_ASSOCIATION, stateChangeUnit.getChangeType());
    }



    public String buildVersionString(NodeRef application) throws Exception {

        StringBuilder builder = new StringBuilder();
        builder.append("\n--------- Version history -------")
                .append("\nCurrent version (also in version history):\n")
                .append("\n\tNodeRef:                        ").append(application);
        Application app = getApplicationBean().getApplication(application);
        if (app.getState() != null) {
            builder.append("\n\tState:                          ").append(app.getState().getTitle());
        }
        builder.append("\n\temail:                          ").append(app.emailTo().getSingleValue());

        VersionHistory history = versionService.getVersionHistory(application);
        if (history != null) {
            Collection<Version> versions = history.getAllVersions();
            builder.append("\n\n\nVersions in version history (").append(versions.size()).append("):");
            for (Version ver : versions) {
                builder.append("\n\nVersion:")
                        .append("\n\tnode-uuid (Props):                                           ").append(ver.getVersionProperties().get("node-uuid"))
                        .append("\n\tFrozenStateNodeRef (getMethod): ").append(ver.getFrozenStateNodeRef())
                        .append("\n")
                        .append("\n\tFrozenStateNodeRef (Props):     ").append(ver.getVersionProperties().get("frozenNodeRef"))
                        .append("\n\tVersionNodeRef (getMethod):     ").append(ver.getVersionedNodeRef())
                        .append("\n\tname (Props):                                           ").append(ver.getVersionProperties().get("name"))
                        .append("\n");

                app = getApplicationBean().getApplication(ver.getFrozenStateNodeRef());
                if (app.getState() != null) {
                    builder.append("\n\tState:                          ").append(app.getState().getTitle());
                }
                builder.append("\n\temail:                          ").append(app.emailTo().getSingleValue());

                //uncomment to log all properties on a version:
                /*
                builder.append("\n");
                for (String s : ver.getVersionProperties().keySet()) {
                    builder.append("\n\t").append(s).append(": ").append(ver.getVersionProperties().get(s));
                }
                */

            }
        }
        builder.append("\n--------------------------------\n\n\n\n");
        return builder.toString();
    }

}
