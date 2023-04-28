package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import lombok.Data;

import lombok.extern.slf4j.Slf4j;

/* For evaluation: Submission navigation backing bean. */
@Slf4j
@ManagedBean(name="submissionNavigation")
@SessionScoped
@Data
public class SubmissionNavigationBean implements Serializable {


    private static final long serialVersionUID = 5517587781720762297L;

    private static final boolean ALL_SUBMISSIONS_DEFAULT = false;

    private String nextGradingId;
    private String currentGradingId;
    private String previousGradingId;
    private Boolean allSubmissions;
    private SelectItem[] otherSubmissionsSelection;


    public SubmissionNavigationBean() {
        log.debug("Creating a new SubmissionNavigationBean");
    }


    public void populate(List<AgentResults> agentResultsList, String currentGradingId) {
        populate(agentResultsList, currentGradingId, ALL_SUBMISSIONS_DEFAULT);
    }

    public void populate(List<AgentResults> agentResultsList, String currentGradingId, boolean allSubmissions) {
        this.allSubmissions = allSubmissions;

        AgentResults currentStudent = agentResultsList.stream()
                .filter(a -> StringUtils.equals(currentGradingId, a.getAssessmentGradingId().toString()))
                .findAny()
                .orElse(null);

        String currentStudentId = currentStudent.getAgentId();


        if (StringUtils.isEmpty(currentStudentId) || agentResultsList.isEmpty()) {
            return;
        }

        agentResultsList = agentResultsList.stream()
                .filter(agent -> agent.getSubmissionCount() > 0)
                .collect(Collectors.toList());

        int currentAgentIndex = -1;
        for (int i = 0; i < agentResultsList.size(); i++) {
            if (StringUtils.equals(currentStudentId, agentResultsList.get(i).getAgentId())) {
                currentAgentIndex = i;
                break;
            }
        }

        log.debug("currentAgentIndex: {}", currentAgentIndex);

        // If current agent is not found or is the first item,
        // or we don't have more then one item
        // we don't have a previous agent
        previousGradingId = currentAgentIndex > 0 && agentResultsList.size() > 1
                ? agentResultsList.get(currentAgentIndex - 1).getAssessmentGradingId().toString()
                : null;

        // If current agent is not found or the last item
        // or we don't have more then one item
        // we don't have a next agent
        nextGradingId = currentAgentIndex != -1 && currentAgentIndex != agentResultsList.size() - 1 && agentResultsList.size() > 1
                ? agentResultsList.get(currentAgentIndex + 1).getAssessmentGradingId().toString()
                : null;

        otherSubmissionsSelection = agentResultsList.stream()
                .map(agent -> {
                    String displayName = agent.getLastName() + ", " + agent.getFirstName()
                        + " (" + agent.getAgentDisplayId()  + ")";
                    return new SelectItem(agent.getAssessmentGradingId(), displayName);
                })
                .toArray(size -> new SelectItem[size]);

    }
}
