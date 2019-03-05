package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

public class TestAction extends ActionExecuterAbstractBase {

    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        try {
            Application change = TestUtils.buildChange(foundationBean.getApplication(actionedUponNodeRef))
                    .changeField("8").setValue(action.getParameterValue("executionMessage")).done()
                    .build();
            foundationBean.updateApplication(change);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    }
}
