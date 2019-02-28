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
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;

import static dk.opendesk.foundationapplication.Utilities.*;


public class VersionTest extends AbstractTestClass {
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");
    private final NodeBean nodeBean = (NodeBean) getServer().getApplicationContext().getBean("nodeBean");

    VersionService versionService = serviceRegistry.getVersionService();

    public VersionTest() {
        super("foundation/application");
    }

    Logger logger = Logger.getLogger(getClass());

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
        String origDesc = foundationBean.getApplication(appRef).getShortDescription();

        assertEquals(1,versionService.getVersionHistory(appRef).getAllVersions().size());

        if (logger.isDebugEnabled()) logger.debug("\nChange #0: Application created\n");
        if (logger.isDebugEnabled()) logger.debug(buildVersionString(appRef));


        // --- FIRST CHANGE --- //
        if (logger.isDebugEnabled()) logger.debug("\nChange #1: Changing the 'description' property\n");

        Application change1 = new Application();
        change1.parseRef(appRef);
        change1.setShortDescription("First change");
        foundationBean.updateApplication(change1);

        //There should now be two versions in the history
        Application headVersion = foundationBean.getApplication(versionService.getVersionHistory(appRef).getHeadVersion().getFrozenStateNodeRef());
        assertEquals(2, versionService.getVersionHistory(appRef).getAllVersions().size());
        assertEquals("First change", headVersion.getShortDescription());

        if (logger.isDebugEnabled()) logger.debug(buildVersionString(appRef));


        // --- SECOND CHANGE --- //
        if (logger.isDebugEnabled()) logger.debug("\nChange #2: Changing the state to 'assess'\n");

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

        if (logger.isDebugEnabled()) logger.debug(buildVersionString(appRef));


        // --- THIRD CHANGE --- //
        if (logger.isDebugEnabled()) logger.debug("\nChange #3: Changing both state and description\n");

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
        Application currentVersion = foundationBean.getApplication(appRef);
        assertEquals(TestUtils.stateAcceptedRef, currentVersion.getState().asNodeRef());
        assertEquals("Third change", currentVersion.getShortDescription());

        if (logger.isDebugEnabled()) logger.debug(buildVersionString(appRef));


        // --- FOURTH CHANGE --- //
        if (logger.isDebugEnabled()) logger.debug("\nChange #4: Application deleted\n");

        foundationBean.deleteApplication(appRef);

        if (logger.isDebugEnabled()) logger.debug(buildVersionString(appRef));


        // --- CALLING foundationBean.getApplicationHistory --- //

        List<ApplicationChange> appChanges = foundationBean.getApplicationHistory(appRef);
        ApplicationChange appChange4 = appChanges.get(0);
        ApplicationChange appChange3 = appChanges.get(1);
        ApplicationChange appChange0 = appChanges.get(4);

        assertEquals(appChange4.getChangeType(), APPLICATION_CHANGE_DELETED);
        assertEquals(appChange4.getChanges().size(), 1);
        assertEquals(appChange4.getChanges().get(0).getChangedField(), STATE_PARAM_TITLE);
        assertEquals(appChange4.getChanges().get(0).getChangeType(), APPLICATION_CHANGE_DELETED);

        assertEquals(appChange3.getChangeType(), APPLICATION_CHANGE_UPDATE);
        assertEquals(appChange3.getChanges().size(), 2);
        assertEquals(appChange3.getChanges().get(0).getChangedField(), STATE_PARAM_TITLE);
        assertEquals(appChange3.getChanges().get(0).getChangeType(), APPLICATION_CHANGE_UPDATE_STATE);

        assertEquals(appChange0.getChangeType(), APPLICATION_CHANGE_CREATED);
        assertEquals(appChange0.getChanges().size(), 3);
        assertEquals(appChange0.getChanges().get(0).getChangedField(), STATE_PARAM_TITLE);
        assertEquals(appChange0.getChanges().get(0).getChangeType(), APPLICATION_CHANGE_CREATED);


        for (ApplicationChange change : appChanges) {
            System.out.println(change);
        }

        //todo : print ogsaa emailchanges og faa dem med i testen


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


    private String buildVersionString(NodeRef application) throws Exception {

        StringBuilder builder = new StringBuilder();
        builder.append("\n--------- Version history -------")
                .append("\nCurrent version (also in version history):\n")
                .append("\n\tNodeRef:                        ").append(application);
        Application app = foundationBean.getApplication(application);
        if (app.getState() != null) {
            builder.append("\n\tState:                          ").append(app.getState().getTitle());
        }
        builder.append("\n\tDescription:                    ").append(app.getShortDescription());
        builder.append("\n\temail:                          ").append(app.getContactEmail());

        VersionHistory history = serviceRegistry.getVersionService().getVersionHistory(application);
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

                app = foundationBean.getApplication(ver.getFrozenStateNodeRef());
                if (app.getState() != null) {
                    builder.append("\n\tState:                          ").append(app.getState().getTitle());
                }
                builder.append("\n\tDescription:                    ").append(app.getShortDescription())
                        .append("\n\temail:                          ").append(app.getContactEmail());

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
