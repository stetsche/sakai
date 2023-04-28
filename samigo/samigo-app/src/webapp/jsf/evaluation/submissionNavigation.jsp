<div class="b5 d-flex gap-1">
    <h:commandLink title="#{evaluationMessages.grading_nav_previous_submission}"
            styleClass="button b5 me-0#{studentScores.previousStudentId == null ? ' disabled' : ''}"
            disabled="#{studentScores.previousStudentId == null}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.GradingNavigationListener" />
        <f:param name="studentId" value="#{submissionNavigation.previousGradingId}" />
        <span aria-hidden="true" class="fa fa-chevron-circle-left"></span>
    </h:commandLink>
    <h:selectOneMenu id="otherStudents" value="#{submissionNavigation.currentGradingId}" onchange="submit()">
        <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
        <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.GradingNavigationListener" />
        <f:selectItems value="#{submissionNavigation.otherSubmissionsSelection}" />
    </h:selectOneMenu>
    <h:commandLink title="#{evaluationMessages.grading_nav_next_submission}"
            styleClass="button#{studentScores.nextStudentId == null ? ' disabled' : ''}"
            disabled="#{studentScores.nextStudentId == null}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.GradingNavigationListener" />
        <f:param name="studentId" value="#{submissionNavigation.nextGradingId}" />
        <span aria-hidden="true" class="fa fa-chevron-circle-right"></span>
    </h:commandLink>
</div>

<%--
 <div class="b5 d-flex gap-1">
    <h:commandLink title="#{evaluationMessages.grading_nav_previous_submission}"
            styleClass="button b5 me-0#{studentScores.previousStudentId == null ? ' disabled' : ''}"
            disabled="#{studentScores.previousStudentId == null}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.GradingNavigationListener" />
        <f:param name="studentId" value="#{studentScores.previousStudentId}" />
        <span aria-hidden="true" class="fa fa-chevron-circle-left"></span>
    </h:commandLink>
    <h:selectOneMenu id="otherStudents" value="#{studentScores.assessmentGradingId}" onchange="submit()">
        <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
        <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.GradingNavigationListener" />
        <f:selectItems value="#{studentScores.otherStudentsSubmissions}" />
    </h:selectOneMenu>
    <h:commandLink title="#{evaluationMessages.grading_nav_next_submission}"
            styleClass="button#{studentScores.nextStudentId == null ? ' disabled' : ''}"
            disabled="#{studentScores.nextStudentId == null}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.GradingNavigationListener" />
        <f:param name="studentId" value="#{studentScores.nextStudentId}" />
        <span aria-hidden="true" class="fa fa-chevron-circle-right"></span>
    </h:commandLink>
</div>
--%>