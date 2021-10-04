<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
</jsp:useBean>

	<sakai:view_container title="#{msgs.title_edit}">
		<sakai:view_content>

<script>includeLatestJQuery('read.jsp');</script>
<link rel="stylesheet" href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" type="text/css" />
<script type="text/javascript" src="/library/js/lang-datepicker/lang-datepicker.js"></script>
<style>
	.ui-datepicker { 
	  margin-left: 100px;
	  z-index: 1000;
	}
</style>

<script>
	jQuery(document).ready(function() {
		localDatePicker({
			input: '#readview\\:dataStartDate',
			useTime: 1,
			parseFormat: 'YYYY-MM-DD HH:mm:ss',
			allowEmptyDate: true,
			val: '<h:outputText value="#{SyllabusTool.syllabusDataStartDate}"/>',
			ashidden: {
					iso8601: 'dataStartDateISO8601'}
		});
		localDatePicker({
			input: '#readview\\:dataEndDate',
			useTime: 1,
			parseFormat: 'YYYY-MM-DD HH:mm:ss',
			allowEmptyDate: true,
			val: '<h:outputText value="#{SyllabusTool.syllabusDataEndDate}"/>',
			ashidden: {
					iso8601: 'dataEndDateISO8601'}
		});

		var menuLink = $('#syllabusMenuBulkEditLink');
		menuLink.addClass('current');
		menuLink.find('a').removeAttr('href');

	});
 </script>
			<h:form id="readview">
				<%@ include file="mainMenu.jsp" %>
				<h:outputText value="#{SyllabusTool.alertMessage}" styleClass="sak-banner-error" rendered="#{SyllabusTool.alertMessage != null}" />

				<div class="page-header">
					<h1><h:outputText value="#{msgs.editNotice}"/></h1>
				</div>

				<h:panelGroup styleClass="form-group">
					<h:outputLabel for="title" styleClass="col-sm-2 col-lg-1">
						<h:outputText value="#{msgs.syllabus_title}"/>
						<h:outputText value=" *" styleClass="highlight"/>
					</h:outputLabel>
					<div class="col-sm-10 col-lg-11">
						<h:inputText styleClass="form-control" value="#{SyllabusTool.syllabusDataTitle}" id="title"/>
					</div>
				</h:panelGroup>

				<h:panelGroup styleClass="form-group col-xs-12">
					<h:outputLabel for="cke_readview:syllabus_compose_read_inputRichText" >
						<h:outputText value="#{msgs.syllabus_content}"/>
					</h:outputLabel>
					<sakai:inputRichText textareaOnly="#{SyllabusTool.mobileSession}" rows="20" cols="120" id="syllabus_compose_read" value="#{SyllabusTool.syllabusDataAsset}" />
				</h:panelGroup>

				<h:panelGroup styleClass="form-group col-xs-12">
				<div class="checkbox">
					<h:selectOneRadio value="#{SyllabusTool.syllabusDataView}"  layout="pageDirection" title="#{msgs.publicPrivate}">
						<f:selectItem itemValue="no" itemLabel="#{msgs.noPrivate}"/>
						<f:selectItem itemValue="yes" itemLabel="#{msgs.yesPublic}"/>
					</h:selectOneRadio>
				</div>
				</h:panelGroup>

				<h2 class="col-xs-12">
					<h:outputText value="#{msgs.attachment}"/>
				</h2>
				<h:panelGroup styleClass="form-group col-xs-12">
				<h:dataTable value="#{SyllabusTool.allAttachments}" var="eachAttach" summary="#{msgs.edit_att_list_summary}" styleClass="table table-striped table-bordered table-hover">
					<h:column rendered="#{!empty SyllabusTool.allAttachments}">
						<f:facet name="header">
							<h:outputText value="#{msgs.attachmentTitle}" />
						</f:facet>
						<f:verbatim><h5></f:verbatim>
						<sakai:contentTypeMap fileType="#{eachAttach.type}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />
						<h:outputText value="#{eachAttach.name}"/>
						<f:verbatim></h5></f:verbatim>
						<f:verbatim><div class="itemAction"></f:verbatim>

						<h:commandLink action="#{SyllabusTool.processDeleteAttach}" title="#{msgs.removeAttachmentLink} #{eachAttach.name}">
							<h:outputText value="#{msgs.mainEditHeaderRemove}"/>
							<f:param value="#{eachAttach.syllabusAttachId}" name="syllabus_current_attach"/>
						</h:commandLink>
						<f:verbatim></div></f:verbatim>
					</h:column>
					<h:column rendered="#{!empty SyllabusTool.allAttachments}">
						<f:facet name="header">
							<h:outputText value="#{msgs.size}" />
						</f:facet>
						<h:outputText value="#{eachAttach.size}"/>
					</h:column>
					<h:column rendered="#{!empty SyllabusTool.allAttachments}">
						<f:facet name="header">
						<h:outputText value="#{msgs.type}" />
						</f:facet>
						<h:outputText value="#{eachAttach.type}"/>
					</h:column>
					<h:column rendered="#{!empty SyllabusTool.allAttachments}">
						<f:facet name="header">
							<h:outputText value="#{msgs.created_by}" />
						</f:facet>
						<h:outputText value="#{eachAttach.createdBy}"/>
					</h:column>
					<h:column rendered="#{!empty SyllabusTool.allAttachments}">
						<f:facet name="header">
							<h:outputText value="#{msgs.last_modified}" />
						</f:facet>
						<h:outputText value="#{eachAttach.lastModifiedBy}"/>
					</h:column>
				</h:dataTable>

				<sakai:button_bar>
					<h:commandButton action="#{SyllabusTool.processAddAttachRedirect}" value="#{msgs.add_attach}"/>
				</sakai:button_bar>
				</h:panelGroup>

				<!-- Date -->
				<h2 class="col-xs-12">
					<h:outputText value="#{msgs.dateheader}"/>
				</h2>
				<h:panelGroup styleClass="form-group">
						<h:outputLabel for="dataStartDate" styleClass="col-sm-2 col-lg-1">
							<h:outputText value="#{msgs.startdatetitle}"/>
						</h:outputLabel>
						<div class="col-sm-10 col-lg-11">
							<h:inputText styleClass="datInputStart" value="#{SyllabusTool.syllabusDataStartDate}" id="dataStartDate"/>
						</div>
				</h:panelGroup>
				<h:panelGroup styleClass="form-group">
						<h:outputLabel for="dataEndDate" styleClass="col-sm-2 col-lg-1">
							<h:outputText value="#{msgs.enddatetitle}"/>
						</h:outputLabel>
						<div class="col-sm-10 col-lg-11">
							<h:inputText styleClass="datInputEnd" value="#{SyllabusTool.syllabusDataEndDate}" id="dataEndDate"/>
						</div>
				</h:panelGroup>
				<h:panelGroup styleClass="form-group col-xs-12" rendered="#{SyllabusTool.calendarExistsForSite}">
					<div class="col-sm-offset-2 col-lg-offset-1 checkbox">
						<h:outputLabel for="linkCalendar">
						<h:selectBooleanCheckbox id="linkCalendar" value="#{SyllabusTool.syllabusDataLinkCalendar}" />
							<h:outputText value="#{msgs.linkcalendartitle}"/>
						</h:outputLabel>
					</div>
				</h:panelGroup>

				<h2 class="col-xs-12">
					<h:outputText value="#{msgs.notificationheader}"/>
				</h2>
				<h:panelGroup styleClass="form-group">
					<h:outputLabel for="list1" styleClass="col-sm-2 col-lg-1">
						<h:outputText value="#{msgs.email_notify}"/>
					</h:outputLabel>
					<div class="col-sm-10 col-lg-11">
						<h:selectOneListbox size = "1"  id = "list1" value="#{SyllabusTool.syllabusDataEmailNotification}" styleClass="form-control">
							<f:selectItem itemLabel="#{msgs.notifyNone}" itemValue="none"/>
							<f:selectItem itemLabel="#{msgs.notifyHigh}" itemValue="high"/>
							<f:selectItem itemLabel="#{msgs.notifyLow}" itemValue="low"/>
						</h:selectOneListbox>
					</div>
				</h:panelGroup>
				<div class="col-xs-12">
					<sakai:button_bar>
						<h:commandButton
							action="#{SyllabusTool.processReadPost}"
							styleClass="active"
							value="#{msgs.bar_publish}"
							accesskey="s" />
						<h:commandButton
							action="#{SyllabusTool.processReadPreview}"
							value="#{msgs.bar_preview}" 
							accesskey="v"	/>
						<h:commandButton
							action="#{SyllabusTool.processReadSave}"
							value="#{msgs.bar_save_draft}" 
							accesskey="d" />
						<h:commandButton
							action="#{SyllabusTool.processReadCancel}"
							value="#{msgs.cancel}" 
							accesskey="x" />
					</sakai:button_bar>
				</div>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
