package dk.opendesk.foundationapplication;

import com.github.sleroy.fakesmtp.core.ServerConfiguration;
import com.github.sleroy.junit.mail.server.MailServer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static dk.opendesk.foundationapplication.EmailTest.TEST_TEMPLATE_NAME;
import static dk.opendesk.foundationapplication.TestUtils.BRANCH_NAME1;
import static dk.opendesk.foundationapplication.TestUtils.BUDGETYEAR1_NAME;
import static dk.opendesk.foundationapplication.TestUtils.BUDGETYEAR2_NAME;
import static dk.opendesk.foundationapplication.TestUtils.SHARED_WORKFLOW_NAME;
import static dk.opendesk.foundationapplication.TestUtils.deleteUsers;
import static dk.opendesk.foundationapplication.TestUtils.getEmptyStringModel;

public class LoadUsersTest extends AbstractTestClass {

    private MailServer mailServer;
    private HashMap<String, Serializable> emptyStringModel = getEmptyStringModel();

    public LoadUsersTest() {
        super("");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();


        mailServer = new MailServer(ServerConfiguration.create().port(2525).charset("UTF-8").relayDomains("testmail.dk"));
        mailServer.start();

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(getServiceRegistry());
        TestUtils.setupSimpleFlow(getServiceRegistry());

        deleteUsers(getServiceRegistry());
    }

    @Override
    protected void tearDown() throws Exception {
        if (mailServer != null) {
            mailServer.close();
        }
        mailServer = null;
        deleteUsers(getServiceRegistry());
        TestUtils.wipeData(getServiceRegistry());
    }


    public void testLoadUsers() throws Exception {
        String groupList = createGroupList();
        String userList = createUserList();
        JSONObject groupJson = new JSONObject(groupList);
        JSONArray userJson = new JSONArray(userList);

        getAuthorityBean().loadUsers(groupJson,userJson,"testSubject", getActionBean().getEmailTemplate(TEST_TEMPLATE_NAME), emptyStringModel);

        NodeRef peopleContainer = getServiceRegistry().getPersonService().getPeopleContainer();
        List<ChildAssociationRef> childAssocs =getServiceRegistry().getNodeService().getChildAssocs(peopleContainer);
        for (ChildAssociationRef c : childAssocs) {
            System.out.println(c.getQName());
            Set<String> auths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(c.getQName().getLocalName());
            for (String auth : auths) {
                System.out.println("\t"+ auth);
            }
        }

        System.out.println("Mails: " + mailServer.getMails().size());
    }

    private String createGroupList() throws JSONException {

        JSONObject groupList = new JSONObject();
        JSONObject permissionsGroup1 = new JSONObject();
        JSONObject permissionsGroup2 = new JSONObject();
        JSONObject permissionsGroup3 = new JSONObject();
        JSONObject permissionsGroup4 = new JSONObject();

        JSONObject workflowPerms = new JSONObject().put(SHARED_WORKFLOW_NAME, "write");
        JSONObject branchPerms = new JSONObject().put(BRANCH_NAME1, "read");
        JSONObject budgetYearPerms = new JSONObject().put(BUDGETYEAR1_NAME,"write").put(BUDGETYEAR2_NAME, "read");


        //todo skal også teste for budgetyear, new_applications
        permissionsGroup1.put("Branch","write");
        permissionsGroup2.put("Workflow",  workflowPerms);
        permissionsGroup3.put("Workflow",  workflowPerms).put("Branch", branchPerms).put("Budget", "read");
        permissionsGroup4.put("Super", "*");

        groupList.put("testSekretær", permissionsGroup1);
        groupList.put("testBestyrelsesmedlem", permissionsGroup2);
        groupList.put("testDirektør", permissionsGroup3);
        groupList.put("testOligark", permissionsGroup4);

        return groupList.toString();
    }

    private String createUserList() throws JSONException {

        JSONArray userList = new JSONArray();
        JSONObject user1 = new JSONObject();
        JSONObject user2 = new JSONObject();
        JSONObject user3 = new JSONObject();
        JSONObject user4 = new JSONObject();


        user1.put("fornavn","Lille");
        user1.put("efternavn","Lise");
        user1.put("email","ll@testmail.dk");
        user1.put("rolle","testSekretær");

        user2.put("fornavn","Foo");
        user2.put("efternavn","Ping");
        user2.put("email","fp@testmail.dk");
        user2.put("rolle","testBestyrelsesmedlem");

        user3.put("fornavn","Tumpe");
        user3.put("efternavn","Lampeskærm");
        user3.put("email","tl@testmail.dk");
        user3.put("rolle","testDirektør");

        user4.put("fornavn","Dumbo");
        user4.put("efternavn","Oliphant");
        user4.put("email","do@testmail.dk");
        user4.put("rolle","testOligark");

        userList.put(user1).put(user2).put(user3).put(user4);
        return userList.toString();

    }

}
