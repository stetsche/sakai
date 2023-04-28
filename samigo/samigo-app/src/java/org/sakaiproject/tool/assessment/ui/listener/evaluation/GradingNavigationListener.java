package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.SubmissionNavigationBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

@Slf4j
public class GradingNavigationListener implements ActionListener, ValueChangeListener  {


    public void processAction(ActionEvent actionEvent) throws AbortProcessingException {
        String studentId = ContextUtil.lookupParam("studentId");
        log.debug("studentId: {}", studentId);

        gradingNavigation(studentId);
    }

    public void processValueChange(ValueChangeEvent valueChangeEvent) throws AbortProcessingException {
        String studentId = (String) valueChangeEvent.getNewValue();
        log.debug("studentId: {}", studentId);

        gradingNavigation(studentId);
    }

    public void gradingNavigation (String gradingId) {
        TotalScoresBean totalScoresBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
        StudentScoresBean studentScoresBean = (StudentScoresBean) ContextUtil.lookupBean("studentScores");
        DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
        SubmissionNavigationBean submissionNavigationBean = (SubmissionNavigationBean) ContextUtil.lookupBean("submissionNavigation");

        List<AgentResults> agents = new ArrayList<>(totalScoresBean.getAgents());
        submissionNavigationBean.populate(agents, gradingId);

        AgentResults agent = agents.stream()
                .filter(a -> StringUtils.equals(gradingId, a.getAssessmentGradingId().toString()))
                .findAny()
                .orElse(null);

        deliveryBean.setNextAssessmentGradingId(Long.valueOf(gradingId));
        StudentScoreListener studentScoreListener = new StudentScoreListener();
        studentScoreListener.studentScores(studentScoresBean.getPublishedId(), agent.getAgentId(),
                gradingId, studentScoresBean.getItemId(), studentScoresBean, true);

        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(
                FacesContext.getCurrentInstance(), null, "studentScores");
    }

    private AgentResults previousAgent(TotalScoresBean totalScoresBean, StudentScoresBean studentScoresBean) {
        Collection<AgentResults> agents = totalScoresBean.getAgents();
        
        return null;
    }


    private AgentResults nextAgent() {
        return null;
    }
}
