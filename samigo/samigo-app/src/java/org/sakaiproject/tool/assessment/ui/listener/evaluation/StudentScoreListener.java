/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.osid.assessment.Assessment;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.SubmissionNavigationBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * This handles the selection of the Student Score page.
 *  </p>
 * <p>Description: Action Listener for Evaluation Student Score page</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Rachel Gollub
 * @version $Id$
 */

@Slf4j
 public class StudentScoreListener
  implements ActionListener
{

  private RubricsService rubricsService = ComponentManager.get(RubricsService.class);

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("StudentScore LISTENER.");
    StudentScoresBean bean = (StudentScoresBean) ContextUtil.lookupBean("studentScores");

    // we probably want to change the poster to be consistent
    String itemId = ContextUtil.lookupParam("itemId");
    String studentId = ContextUtil.lookupParam("studentid");
    String gradingId = ContextUtil.lookupParam("gradingData");
    String publishedId = ContextUtil.lookupParam("publishedIdd");
    log.debug("itemId: {}", itemId);
    log.debug("studentId: {}", studentId);
    log.debug("gradingId: {}", gradingId);
    log.debug("publishedId: {}", publishedId);
    
    if (!studentScores(publishedId, studentId, gradingId, itemId, bean, false))
    {
      throw new RuntimeException("failed to call studentScores.");
    }

  }

  /**
   * This will populate the StudentScoresBean with the data associated with the
   * particular versioned assessment based on the publishedId.
   *
   * @param publishedId String
   * @param bean StudentScoresBean
   * @return boolean
   */
  public boolean studentScores(String publishedId, String studentId, String gradingId, String itemId,
      StudentScoresBean bean, boolean isValueChange) {
    log.debug("studentScores()");
    try
    {
//  SAK-4121, do not pass studentName as f:param, will cause javascript error if name contains apostrophe 
//    bean.setStudentName(cu.lookupParam("studentName"));

      bean.setPublishedId(publishedId);
      bean.setStudentId(studentId);
      AgentFacade agent = new AgentFacade(studentId);
      bean.setStudentName(agent.getFirstName() + " " + agent.getLastName());
      bean.setLastName(agent.getLastName());
      bean.setFirstName(agent.getFirstName());
      bean.setAssessmentGradingId(gradingId);
      bean.setItemId(itemId);
      bean.setEmail(agent.getEmail());
      bean.setDisplayId(agent.getDisplayIdString());

      populateNavigation(bean, studentId);

      DeliveryBean dbean = (DeliveryBean) ContextUtil.lookupBean("delivery");
      dbean.setActionString("gradeAssessment");

      DeliveryActionListener listener = new DeliveryActionListener();
      listener.processAction(null);
      
      // Added for SAK-13930
      DeliveryBean updatedDeliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
      List<SectionContentsBean> parts = updatedDeliveryBean.getPageContents().getPartsContents();
      for (SectionContentsBean part : parts) {
        List<ItemContentsBean> items = part.getItemContents();
        for (ItemContentsBean question : items) {
          question.setRubricStateDetails("");
          if (question.getGradingComment() != null && !question.getGradingComment().equals("")) {
            question.setGradingComment(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(question.getGradingComment()));
          }
        }
      } // End of SAK-13930

      GradingService service = new GradingService();
      AssessmentGradingData adata= (AssessmentGradingData) service.load(bean.getAssessmentGradingId(), false);
      bean.setComments(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(adata.getComments()));
      buildItemContentsMap(dbean, publishedId);

      return true;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  private void populateNavigation(StudentScoresBean studentScoresBean, String currentStudentId) {
    TotalScoresBean totalScoresBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
    log.debug("totalScores: {}", totalScoresBean);

    if (StringUtils.isEmpty(currentStudentId) || totalScoresBean.getAgents() == null) {
      log.debug("early 1");
      return;
    }

    List<AgentResults> agentList = ((Collection<AgentResults>) totalScoresBean.getAgents()).stream()
            .filter(agent -> agent.getSubmissionCount() > 0)
            .collect(Collectors.toList());

    int currentAgentIndex = -1;
    for (int i = 0; i < agentList.size(); i++) {
      if (StringUtils.equals(currentStudentId, agentList.get(i).getAgentId())) {
        currentAgentIndex = i;
        break;
      }
    }
    log.debug("currentAgentIndex: {}", currentAgentIndex);

    AgentResults currentAgent = agentList.get(currentAgentIndex);
    SubmissionNavigationBean submissionNavigationBean = (SubmissionNavigationBean) ContextUtil.lookupBean("submissionNavigation");
    submissionNavigationBean.populate(agentList, currentAgent.getAssessmentGradingId().toString());
    // If current agent is not found or is the first item,
    // or we don't have more then one item
    // we don't have a previous agent
    studentScoresBean.setPreviousStudentId(currentAgentIndex > 0 && agentList.size() > 1
        ? agentList.get(currentAgentIndex - 1).getAssessmentGradingId().toString()
        : null);

    // If current agent is not found or the last item
    // or we don't have more then one item
    // we don't have a next agent
    studentScoresBean.setNextStudentId(currentAgentIndex != -1 && currentAgentIndex != agentList.size() - 1 && agentList.size() > 1
        ? agentList.get(currentAgentIndex + 1).getAssessmentGradingId().toString()
        : null);

    studentScoresBean.setOtherStudentsSubmissions(agentList.stream()
        .map(agent -> {
            String displayName = agent.getLastName() + ", " + agent.getFirstName()
                + " (" + agent.getAgentDisplayId()  + ")";
            return new SelectItem(agent.getAssessmentGradingId(), displayName);
        })
        .toArray(size -> new SelectItem[size]));

    for (int i = 0; i < agentList.size(); i++) {
      String agent = agentList.get(i).getAgentId();
      AgentResults agentA = agentList.get(i);

      if (i == currentAgentIndex) {
        log.debug("{} c {} {}",i, agent, isGraded(agentA));
      } else if(studentScoresBean.getPreviousStudentId() == agent) {
        log.debug("{} p {} {}",i, agent, isGraded(agentA));
      } else if(studentScoresBean.getNextStudentId() == agent) {
        log.debug("{} n {} {}",i, agent, isGraded(agentA));
      } else {
        log.debug("{} - {} {}",i, agent, isGraded(agentA));
      }
    }
  }
  
  private static boolean isGraded(AgentResults agentResults) {
    AssessmentGradingData assessmentGradingData = agentResults.getAssessmentGrading();

      log.debug("agent.forGrade: {}", agentResults.getForGrade());
      log.debug("agent.status: {}", agentResults.getStatus());
      log.debug("status: {}", assessmentGradingData.getStatus());
      log.debug("forGrade: {}", assessmentGradingData.getForGrade());
      log.debug("gradedBy: {}", assessmentGradingData.getGradedBy());
      log.debug("gradedDate: {}", assessmentGradingData.getGradedDate());

    return assessmentGradingData != null && assessmentGradingData.getGradedDate() != null;
  }

  private void buildItemContentsMap(DeliveryBean dbean, String publishedId) {
	  Map<Long, ItemContentsBean> itemContentsMap = new HashMap<>();

      dbean.getPageContents().getPartsContents().stream()
              .filter(Objects::nonNull)
              .forEach(p -> p.getItemContents().stream()
                      .filter(Objects::nonNull)
                      .forEach(i -> {
                          i.setHasAssociatedRubric(rubricsService.hasAssociatedRubric(RubricsConstants.RBCS_TOOL_SAMIGO, RubricsConstants.RBCS_PUBLISHED_ASSESSMENT_ENTITY_PREFIX + publishedId + "." + i.getItemData().getItemId()));
                          i.getItemGradingDataArray()
                                  .forEach(d -> itemContentsMap.put(d.getItemGradingId(), i));
                      }));

	  dbean.setItemContentsMap(itemContentsMap);
  }
}
