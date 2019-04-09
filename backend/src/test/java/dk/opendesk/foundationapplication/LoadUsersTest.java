package dk.opendesk.foundationapplication;

import com.github.sleroy.fakesmtp.core.ServerConfiguration;
import com.github.sleroy.junit.mail.server.MailServer;
import dk.opendesk.foundationapplication.enums.PermissionGroup;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static dk.opendesk.foundationapplication.EmailTest.TEST_TEMPLATE_NAME;
import static dk.opendesk.foundationapplication.TestUtils.APPLICATION3_NAME;
import static dk.opendesk.foundationapplication.TestUtils.BRANCH_NAME1;
import static dk.opendesk.foundationapplication.TestUtils.BUDGET1_NAME;
import static dk.opendesk.foundationapplication.TestUtils.BUDGET2_NAME;
import static dk.opendesk.foundationapplication.TestUtils.BUDGETYEAR1_NAME;
import static dk.opendesk.foundationapplication.TestUtils.SHARED_WORKFLOW_NAME;
import static dk.opendesk.foundationapplication.TestUtils.branchRef1;
import static dk.opendesk.foundationapplication.TestUtils.budgetRef1;
import static dk.opendesk.foundationapplication.TestUtils.budgetRef2;
import static dk.opendesk.foundationapplication.TestUtils.deleteUsers;
import static dk.opendesk.foundationapplication.TestUtils.getEmptyStringModel;
import static dk.opendesk.foundationapplication.TestUtils.workFlowRef1;

public class LoadUsersTest extends AbstractTestClass {

    private MailServer mailServer;
    private HashMap<String, Serializable> emptyStringModel = getEmptyStringModel();

    public LoadUsersTest() {
        super("/foundation/groupsandusers");
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

    public void testStartUsersWebScript() throws JSONException, IOException {
        String groupList = createGroupListSimpleRead();
        String userList = createUserList();

        JSONObject data = new JSONObject().put("groups", new JSONObject(groupList)).put("users", new JSONArray(userList));

        post(data);

        assertEquals(5, mailServer.getMails().size());

        System.out.println(mailServer.getMails().get(0));
    }

    public void testSendMails() throws Exception {
        String groupList = createGroupListSimpleRead();
        String userList = createUserList();
        JSONObject groupJson = new JSONObject(groupList);
        JSONArray userJson = new JSONArray(userList);

        getAuthorityBean().loadUsers(groupJson,userJson,"testSubject", getActionBean().getEmailTemplate(TEST_TEMPLATE_NAME), emptyStringModel);

        assertEquals(5, mailServer.getMails().size());

        String mail = mailServer.getMails().get(0).getEmailStr();
        String userNameLine = null;
        String passWordLine = null;
        String emailLine = null;
        String subjectLine = null;
        String roleLine = null;
        String firstNameLine = null;
        String lastNameLine = null;
        String phoneLine = null;
        for (String s : mail.split("\n")) {
            if (s.startsWith("userName =")) {
                userNameLine = s;
            }
            if (s.startsWith("password =")) {
                passWordLine = s;
            }
            if (s.startsWith("email =")) {
                emailLine = s;
            }
            if (s.startsWith("subject =")) {
                subjectLine = s;
            }
            if (s.startsWith("role =")) {
                roleLine = s;
            }
            if (s.startsWith("firstName =")) {
                firstNameLine = s;
            }
            if (s.startsWith("lastName =")) {
                lastNameLine = s;
            }
            if (s.startsWith("phone =")) {
                phoneLine = s;
            }
        }
        assertEquals("userName =3D Lille_Lise", userNameLine);
        assertEquals("subject =3D testSubject",subjectLine);
        assertEquals("phone =3D *** telefonnummer mangler ***", phoneLine);
        assertEquals("role =3D testSekret=C3=A6r", roleLine);
        assertEquals("email =3D ll@testmail.dk", emailLine);
        assertEquals("firstName =3D Lille", firstNameLine);
        assertEquals("lastName =3D Lise", lastNameLine);
        assertEquals(8, passWordLine.replace("password =3D ","").length());
    }

    public void testLoadUsersSimpleRead() throws Exception {
        String groupList = createGroupListSimpleRead();
        String userList = createUserList();
        JSONObject groupJson = new JSONObject(groupList);
        JSONArray userJson = new JSONArray(userList);

        getAuthorityBean().loadUsers(groupJson,userJson,"testSubject", getActionBean().getEmailTemplate(TEST_TEMPLATE_NAME), emptyStringModel);

        NodeRef peopleContainer = getServiceRegistry().getPersonService().getPeopleContainer();
        List<ChildAssociationRef> childAssocs =getServiceRegistry().getNodeService().getChildAssocs(peopleContainer);
        String sekretaryUser = null;
        String boardmemberUser = null;
        String bossUser = null;
        String oligarkUser = null;
        String minimalUser = null;

        for (ChildAssociationRef c : childAssocs) {
            String userName = c.getQName().getLocalName();
            if (userName.equals("lille_lise")) {
                sekretaryUser = userName;
            }
            if (userName.equals("dumbo_oliphant")) {
                oligarkUser = userName;
            }
            if (userName.equals("tumpe_lampeskærm")) {
                bossUser = userName;
            }
            if (userName.equals("foo_ping")) {
                boardmemberUser = userName;
            }
            if (userName.equals("mini_man")) {
                minimalUser = userName;
            }
        }

        Set<String> sekretaryAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(sekretaryUser);
        Set<String> boardmemberAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(boardmemberUser);
        Set<String> bossAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(bossUser);
        Set<String> oligarkAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(oligarkUser);
        Set<String> minimalAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(minimalUser);

        //Branch auth also gives workflow and budget
        assertEquals(7, sekretaryAuths.size());
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.BRANCH, null, false)));
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.BRANCH, branchRef1, false)));
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.WORKFLOW, workFlowRef1, false)));
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.BUDGET, budgetRef1, false)));
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.BUDGET, budgetRef2, false)));

        assertEquals(4, boardmemberAuths.size());
        assertTrue(boardmemberAuths.contains(getAuthString(PermissionGroup.WORKFLOW, null, false)));
        assertTrue(boardmemberAuths.contains(getAuthString(PermissionGroup.WORKFLOW, TestUtils.workFlowRef1, false)));

        assertEquals(4, bossAuths.size());
        assertTrue(bossAuths.contains(getAuthString(PermissionGroup.BUDGET_YEAR, null, false)));
        assertTrue(bossAuths.contains(getAuthString(PermissionGroup.BUDGET_YEAR, TestUtils.budgetYearRef1, false)));

        assertEquals(5, oligarkAuths.size());
        assertTrue(oligarkAuths.contains(getAuthString(PermissionGroup.BUDGET, null, false)));
        assertTrue(oligarkAuths.contains(getAuthString(PermissionGroup.BUDGET, budgetRef1, false)));
        assertTrue(oligarkAuths.contains(getAuthString(PermissionGroup.BUDGET, budgetRef2, false)));

        assertEquals(3, minimalAuths.size());
        assertTrue(minimalAuths.contains(getAuthString(PermissionGroup.NEW_APPLICATION, null, false)));
    }



    private String createGroupListSimpleRead() throws JSONException {

        JSONObject groupList = new JSONObject();
        JSONObject permissionsGroup1 = new JSONObject();
        JSONObject permissionsGroup2 = new JSONObject();
        JSONObject permissionsGroup3 = new JSONObject();
        JSONObject permissionsGroup4 = new JSONObject();
        JSONObject permissionsGroup5 = new JSONObject();

        permissionsGroup1.put("Branch","read");
        permissionsGroup2.put("Workflow", "read");
        permissionsGroup3.put("BudgetYear", "read");
        permissionsGroup4.put("Budget", "read");
        permissionsGroup5.put("NewApplication", "read");

        groupList.put("testSekretær", permissionsGroup1);
        groupList.put("testBestyrelsesmedlem", permissionsGroup2);
        groupList.put("testDirektør", permissionsGroup3);
        groupList.put("testOligark", permissionsGroup4);
        groupList.put("testMinimal", permissionsGroup5);

        return groupList.toString();
    }

    public void testLoadUsersSimpleWrite() throws Exception {
        String groupList = createGroupListSimpleWrite();
        String userList = createUserList();
        JSONObject groupJson = new JSONObject(groupList);
        JSONArray userJson = new JSONArray(userList);

        getAuthorityBean().loadUsers(groupJson,userJson,"testSubject", getActionBean().getEmailTemplate(TEST_TEMPLATE_NAME), emptyStringModel);

        NodeRef peopleContainer = getServiceRegistry().getPersonService().getPeopleContainer();
        List<ChildAssociationRef> childAssocs =getServiceRegistry().getNodeService().getChildAssocs(peopleContainer);
        String sekretaryUser = null;
        String boardmemberUser = null;
        String bossUser = null;
        String oligarkUser = null;
        String minimalUser = null;

        for (ChildAssociationRef c : childAssocs) {
            String userName = c.getQName().getLocalName();
            if (userName.equals("lille_lise")) {
                sekretaryUser = userName;
            }
            if (userName.equals("dumbo_oliphant")) {
                oligarkUser = userName;
            }
            if (userName.equals("tumpe_lampeskærm")) {
                bossUser = userName;
            }
            if (userName.equals("foo_ping")) {
                boardmemberUser = userName;
            }
            if (userName.equals("mini_man")) {
                minimalUser = userName;
            }
        }

        Set<String> sekretaryAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(sekretaryUser);
        Set<String> boardmemberAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(boardmemberUser);
        Set<String> bossAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(bossUser);
        Set<String> oligarkAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(oligarkUser);
        Set<String> minimalAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(minimalUser);

        //Branch auth also gives workflow and budget
        assertEquals(10, sekretaryAuths.size());
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.BRANCH, null, true)));
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.BRANCH, null, false)));
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.BRANCH, branchRef1, true)));
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.BRANCH, branchRef1, false)));
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.WORKFLOW, workFlowRef1, true)));
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.WORKFLOW, workFlowRef1, false)));
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.BUDGET, budgetRef1, false)));
        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.BUDGET, budgetRef2, false)));

        assertEquals(6, boardmemberAuths.size());
        assertTrue(boardmemberAuths.contains(getAuthString(PermissionGroup.WORKFLOW, null, true)));
        assertTrue(boardmemberAuths.contains(getAuthString(PermissionGroup.WORKFLOW, null, false)));
        assertTrue(boardmemberAuths.contains(getAuthString(PermissionGroup.WORKFLOW, TestUtils.workFlowRef1, true)));
        assertTrue(boardmemberAuths.contains(getAuthString(PermissionGroup.WORKFLOW, TestUtils.workFlowRef1, false)));

        assertEquals(6, bossAuths.size());
        assertTrue(bossAuths.contains(getAuthString(PermissionGroup.BUDGET_YEAR, null, true)));
        assertTrue(bossAuths.contains(getAuthString(PermissionGroup.BUDGET_YEAR, null, false)));
        assertTrue(bossAuths.contains(getAuthString(PermissionGroup.BUDGET_YEAR, TestUtils.budgetYearRef1, true)));
        assertTrue(bossAuths.contains(getAuthString(PermissionGroup.BUDGET_YEAR, TestUtils.budgetYearRef1, false)));

        assertEquals(8, oligarkAuths.size());
        assertTrue(oligarkAuths.contains(getAuthString(PermissionGroup.BUDGET, null, true)));
        assertTrue(oligarkAuths.contains(getAuthString(PermissionGroup.BUDGET, null, false)));
        assertTrue(oligarkAuths.contains(getAuthString(PermissionGroup.BUDGET, budgetRef1, true)));
        assertTrue(oligarkAuths.contains(getAuthString(PermissionGroup.BUDGET, budgetRef1, false)));
        assertTrue(oligarkAuths.contains(getAuthString(PermissionGroup.BUDGET, budgetRef2, true)));
        assertTrue(oligarkAuths.contains(getAuthString(PermissionGroup.BUDGET, budgetRef2, false)));

        assertEquals( 4, minimalAuths.size());
        assertTrue(minimalAuths.contains(getAuthString(PermissionGroup.NEW_APPLICATION, null, true)));
        assertTrue(minimalAuths.contains(getAuthString(PermissionGroup.NEW_APPLICATION, null, false)));
    }



    private String createGroupListSimpleWrite() throws JSONException {

        JSONObject groupList = new JSONObject();
        JSONObject permissionsGroup1 = new JSONObject();
        JSONObject permissionsGroup2 = new JSONObject();
        JSONObject permissionsGroup3 = new JSONObject();
        JSONObject permissionsGroup4 = new JSONObject();
        JSONObject permissionsGroup5 = new JSONObject();

        permissionsGroup1.put("Branch","write");
        permissionsGroup2.put("Workflow", "write");
        permissionsGroup3.put("BudgetYear", "write");
        permissionsGroup4.put("Budget", "write");
        permissionsGroup5.put("NewApplication", "write");

        groupList.put("testSekretær", permissionsGroup1);
        groupList.put("testBestyrelsesmedlem", permissionsGroup2);
        groupList.put("testDirektør", permissionsGroup3);
        groupList.put("testOligark", permissionsGroup4);
        groupList.put("testMinimal", permissionsGroup5);

        return groupList.toString();
    }


    public void testLoadUsersComplex() throws Exception {
        String groupList = createGroupListComplex();
        String userList = createUserList();
        JSONObject groupJson = new JSONObject(groupList);
        JSONArray userJson = new JSONArray(userList);

        getAuthorityBean().loadUsers(groupJson,userJson,"testSubject", getActionBean().getEmailTemplate(TEST_TEMPLATE_NAME), emptyStringModel);

        NodeRef peopleContainer = getServiceRegistry().getPersonService().getPeopleContainer();
        List<ChildAssociationRef> childAssocs =getServiceRegistry().getNodeService().getChildAssocs(peopleContainer);
        String sekretaryUser = null;
        String boardmemberUser = null;
        String bossUser = null;
        String oligarkUser = null;
        String minimalUser = null;

        for (ChildAssociationRef c : childAssocs) {
            String userName = c.getQName().getLocalName();
            if (userName.equals("lille_lise")) {
                sekretaryUser = userName;
            }
            if (userName.equals("dumbo_oliphant")) {
                oligarkUser = userName;
            }
            if (userName.equals("tumpe_lampeskærm")) {
                bossUser = userName;
            }
            if (userName.equals("foo_ping")) {
                boardmemberUser = userName;
            }
            if (userName.equals("mini_man")) {
                minimalUser = userName;
            }
        }

        Set<String> sekretaryAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(sekretaryUser);
        Set<String> boardmemberAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(boardmemberUser);
        Set<String> bossAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(bossUser);
        Set<String> oligarkAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(oligarkUser);
        Set<String> minimalAuths = getServiceRegistry().getAuthorityService().getAuthoritiesForUser(minimalUser);

        assertTrue(sekretaryAuths.contains(getAuthString(PermissionGroup.BRANCH, null, true)));

        assertTrue(boardmemberAuths.contains(getAuthString(PermissionGroup.WORKFLOW, TestUtils.workFlowRef1, true)));
        assertTrue(boardmemberAuths.contains(getAuthString(PermissionGroup.BUDGET, TestUtils.budgetRef1, true)));
        assertTrue(boardmemberAuths.contains(getAuthString(PermissionGroup.BUDGET, budgetRef2, false)));

        assertTrue(bossAuths.contains(getAuthString(PermissionGroup.BUDGET_YEAR, TestUtils.budgetYearRef1, true)));
        assertTrue(bossAuths.contains(getAuthString(PermissionGroup.BRANCH, TestUtils.branchRef1, false)));
        assertTrue(bossAuths.contains(getAuthString(PermissionGroup.BUDGET, null, false)));
        //assertTrue(bossAuths.contains(getAuthString(PermissionGroup.NEW_APPLICATION, application3, false)));

        assertTrue(oligarkAuths.contains(getAuthString(PermissionGroup.SUPER, null, true)));

        assertEquals(2, minimalAuths.size());

    }

    private String createGroupListComplex() throws JSONException {

        JSONObject groupList = new JSONObject();
        JSONObject permissionsGroup1 = new JSONObject();
        JSONObject permissionsGroup2 = new JSONObject();
        JSONObject permissionsGroup3 = new JSONObject();
        JSONObject permissionsGroup4 = new JSONObject();

        JSONObject workflowPerms = new JSONObject().put(SHARED_WORKFLOW_NAME, "write");
        JSONObject branchPerms = new JSONObject().put(BRANCH_NAME1, "read");
        JSONObject budgetYearPerms = new JSONObject().put(BUDGETYEAR1_NAME,"write");
        JSONObject budgetPerms = new JSONObject().put(BUDGET1_NAME, "write").put(BUDGET2_NAME, "read");
        JSONObject newAppPerms = new JSONObject().put(APPLICATION3_NAME, "read");

        permissionsGroup1.put("Branch","write");
        permissionsGroup2.put("Workflow",  workflowPerms).put("Budget", budgetPerms);
        permissionsGroup3.put("BudgetYear",  budgetYearPerms).put("Branch", branchPerms).put("Budget", "read"); //.put("NewApplication", newAppPerms);
        permissionsGroup4.put("Super", "*");

        groupList.put("testSekretær", permissionsGroup1);
        groupList.put("testBestyrelsesmedlem", permissionsGroup2);
        groupList.put("testDirektør", permissionsGroup3);
        groupList.put("testOligark", permissionsGroup4);
        groupList.put("testMinimal", new JSONObject());

        //System.out.println(groupList.toString());
        return groupList.toString();
    }

    private String createUserList() throws JSONException {

        JSONArray userList = new JSONArray();
        JSONObject user1 = new JSONObject();
        JSONObject user2 = new JSONObject();
        JSONObject user3 = new JSONObject();
        JSONObject user4 = new JSONObject();
        JSONObject user5 = new JSONObject();


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

        user5.put("fornavn","Mini");
        user5.put("efternavn","Man");
        user5.put("email","mm@testmail.dk");
        user5.put("rolle","testMinimal");

        userList.put(user1).put(user2).put(user3).put(user4).put(user5);
        //System.out.println(userList);
        return userList.toString();

    }

    public String getAuthString(PermissionGroup group, NodeRef subName, boolean write) {

        String shortName = group.getShortName(subName) +(!write ? "_Read" : "");
        return getServiceRegistry().getAuthorityService().getName(AuthorityType.GROUP, shortName);
    }
}
