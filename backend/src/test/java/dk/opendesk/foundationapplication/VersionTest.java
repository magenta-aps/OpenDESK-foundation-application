package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
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

    public void testVersioning() throws Exception {

        NodeRef appRef = TestUtils.application1;
        String origMail = getApplicationBean().getApplication(appRef).emailTo().getValue();
        String origStateTitle = getApplicationBean().getApplication(appRef).getState().getTitle();

        assertEquals(1,versionService.getVersionHistory(appRef).getAllVersions().size());

        if (logger.isDebugEnabled()) logger.debug("\nChange #0: Application created\n");
        if (logger.isDebugEnabled()) logger.debug(getApplicationBean().getApplicationHistory(appRef));


        // --- FIRST CHANGE --- //
        if (logger.isDebugEnabled()) logger.debug("\nChange #1: Changing the 'description' property\n");

        Application change1 = Utilities.buildChange(getApplicationBean().getApplication(appRef))
                .changeField("8").setValue("First change").done()
                .build();
        getApplicationBean().updateApplication(change1);

        //There should now be two versions in the history
        if (logger.isDebugEnabled()) logger.debug(getApplicationBean().getApplicationHistory(appRef));
        Application headVersion = getApplicationBean().getApplication(versionService.getVersionHistory(appRef).getHeadVersion().getFrozenStateNodeRef());
        assertEquals(2, versionService.getVersionHistory(appRef).getAllVersions().size());
        assertEquals("First change", headVersion.emailTo().getValue());



        // --- SECOND CHANGE --- //
        if (logger.isDebugEnabled()) logger.debug("\nChange #2: Changing the state to 'assess'\n");

        Application change2 = new Application();
        change2.parseRef(appRef);
        StateReference stateAssess = new StateReference();
        stateAssess.parseRef(TestUtils.stateAccessRef);
        change2.setState(stateAssess);
        getApplicationBean().updateApplication(change2);

        //There should now be two versions in the history and the newest on should be on state 'assess' and with desc = 'First change'
        headVersion = getApplicationBean().getApplication(versionService.getVersionHistory(appRef).getHeadVersion().getFrozenStateNodeRef());
        assertEquals(3, versionService.getVersionHistory(appRef).getAllVersions().size());
        assertEquals(TestUtils.stateAccessRef, headVersion.getState().asNodeRef());
        assertEquals("First change", headVersion.emailTo().getValue());

        if (logger.isDebugEnabled()) logger.debug(getApplicationBean().getApplicationHistory(appRef));


        // --- THIRD CHANGE --- //
        if (logger.isDebugEnabled()) logger.debug("\nChange #3: Changing both state and description\n");

        Application change3 = Utilities.buildChange(getApplicationBean().getApplication(appRef))
                .changeField("8").setValue("Third change").done()
                .build();
        StateReference stateAccepted = new StateReference();
        stateAccepted.parseRef(TestUtils.stateAcceptedRef);
        change3.setState(stateAccepted);
        getApplicationBean().updateApplication(change3);

        //There should now be three versions and the newest one should be on state 'accepted' and have description = 'Third change'
        headVersion = getApplicationBean().getApplication(versionService.getVersionHistory(appRef).getHeadVersion().getFrozenStateNodeRef());
        assertEquals(4, versionService.getVersionHistory(appRef).getAllVersions().size());
        assertEquals(TestUtils.stateAcceptedRef, headVersion.getState().asNodeRef());
        assertEquals("Third change", headVersion.emailTo().getValue());

        //Current version should be on state 'accepted' and have description = 'Third change'
        Application currentVersion = getApplicationBean().getApplication(appRef);
        assertEquals(TestUtils.stateAcceptedRef, currentVersion.getState().asNodeRef());
        assertEquals("Third change", currentVersion.emailTo().getValue());

        if (logger.isDebugEnabled()) logger.debug(getApplicationBean().getApplicationHistory(appRef));


        // --- FOURTH CHANGE --- //
        if (logger.isDebugEnabled()) logger.debug("\nChange #4: Application deleted\n");

        getApplicationBean().deleteApplication(appRef);

        if (logger.isDebugEnabled()) logger.debug(getApplicationBean().getApplicationHistory(appRef));


        // --- CALLING foundationBean.getApplicationHistory --- //

        List<ApplicationChange> appChanges = getApplicationBean().getApplicationHistory(appRef);
        //System.out.println(appChanges);
        ApplicationChange appChange4 = appChanges.get(4);
        ApplicationChange appChange3 = appChanges.get(3);
        ApplicationChange appChange0 = appChanges.get(0);

        assertEquals(APPLICATION_CHANGE_DELETED, appChange4.getChangeType());
        assertEquals(4, appChange4.getChanges().size());
        assertEquals("Branch", appChange4.getChanges().get(0).getChangedField());
        assertEquals(APPLICATION_CHANGE_UPDATE_ASSOCIATION, appChange4.getChanges().get(0).getChangeType());

        assertEquals(APPLICATION_CHANGE_UPDATE, appChange3.getChangeType());
        assertEquals(2, appChange3.getChanges().size());
        assertEquals("State", appChange3.getChanges().get(1).getChangedField());
        assertEquals(APPLICATION_CHANGE_UPDATE_ASSOCIATION, appChange3.getChanges().get(1).getChangeType());

        assertEquals(appChange0.getChangeType(), APPLICATION_CHANGE_CREATED);

        ApplicationChangeUnit stateChange = null;
        ApplicationChangeUnit emailChange = null;
        for (ApplicationChangeUnit unit : appChange0.getChanges()) {
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


        //todo: det følgende fejler pga noget jackson
        /*
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
        assertEquals(APPLICATION_CHANGE_UPDATE_PROP, changeUnits.get(0).getChangeType());

        //Testing the second change
        changeUnits = changeLists.get(1).getChanges();
        assertEquals(1, changeUnits.size());
        assertEquals(STATE_PARAM_TITLE, changeUnits.get(0).getChangedField());
        assertEquals(foundationBean.getState(TestUtils.stateRecievedRef).getTitle(), changeUnits.get(0).getOldValue());
        assertEquals(foundationBean.getState(TestUtils.stateAccessRef).getTitle(), changeUnits.get(0).getNewValue());
        assertEquals(APPLICATION_CHANGE_UPDATE_STATE, changeUnits.get(0).getChangeType());

        //Testing the third change
        changeUnits = changeLists.get(0).getChanges();
        assertEquals(2, changeUnits.size());
        assertEquals(STATE_PARAM_TITLE, changeUnits.get(0).getChangedField());
        assertEquals(foundationBean.getState(TestUtils.stateAccessRef).getTitle(), changeUnits.get(0).getOldValue());
        assertEquals(foundationBean.getState(TestUtils.stateAcceptedRef).getTitle(), changeUnits.get(0).getNewValue());
        assertEquals(APPLICATION_CHANGE_UPDATE_STATE, changeUnits.get(0).getChangeType());
        assertEquals(APPLICATION_PARAM_SHORT_DESCRIPTION, changeUnits.get(1).getChangedField());
        assertEquals("First change", changeUnits.get(1).getOldValue());
        assertEquals("Third change", changeUnits.get(1).getNewValue());
        assertEquals(APPLICATION_CHANGE_UPDATE_PROP, changeUnits.get(1).getChangeType());

        */
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
        builder.append("\n\temail:                          ").append(app.emailTo().getValue());

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
                builder.append("\n\temail:                          ").append(app.emailTo().getValue());

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
