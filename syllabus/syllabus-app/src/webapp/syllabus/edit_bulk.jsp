<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
	<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
		<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
	</jsp:useBean>
	<sakai:view_container title="#{msgs.bar_create_edit}">
		<sakai:view_content>

<script>includeLatestJQuery('edit_bulk.jsp');</script>
<link rel="stylesheet" href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" type="text/css" />
<script type="text/javascript" src="/library/js/lang-datepicker/lang-datepicker.js"></script>


	<script>
		jQuery(document).ready(function() {
			localDatePicker({
				input: '#syllabusEdit\\:dataStartDate',
				useTime: 0,
				parseFormat: 'YYYY-MM-DD',
				allowEmptyDate: true,
				val: '<h:outputText value="#{SyllabusTool.bulkEntry.startDate}"><f:convertDateTime pattern="yyyy-MM-dd"/></h:outputText>',
				ashidden: {iso8601: 'dataStartDateISO8601'}
			});
			localDatePicker({
				input: '#syllabusEdit\\:dataEndDate',
				useTime: 0,
				parseFormat: 'YYYY-MM-DD',
				allowEmptyDate: true,
				val: '<h:outputText value="#{SyllabusTool.bulkEntry.endDate}"><f:convertDateTime pattern="yyyy-MM-dd"/></h:outputText>',
				ashidden: {iso8601: 'dataEndDateISO8601'}
			});

			var menuLink = $('#syllabusMenuBulkAddItemLink');
			menuLink.addClass('current');
			menuLink.find('a').removeAttr('href');

		});
		$(function() {
			$('.timeInput').timepicker({
		    	hour: 8,
		    	defaultValue: "08:00 <h:outputText value="#{msgs.am}"/>",
		    	timeOnlyTitle: "<h:outputText value="#{msgs.choose_time}"/>",
				timeFormat: "hh:mm tt",
				currentText: "<h:outputText value="#{msgs.now}"/>",
				closeText: "<h:outputText value="#{msgs.done}"/>",
				amNames: ['<h:outputText value="#{msgs.am}"/>', '<h:outputText value="#{msgs.am2}"/>'],
				pmNames: ['<h:outputText value="#{msgs.pm}"/>', '<h:outputText value="#{msgs.pm2}"/>'],
				timeText: "<h:outputText value="#{msgs.time}"/>",
				hourText: "<h:outputText value="#{msgs.hour}"/>",
				minuteText: "<h:outputText value="#{msgs.minute}"/>",
				beforeShow: function (textbox, instance) {
					            instance.dpDiv.css({
					                    marginLeft: textbox.offsetWidth + 'px'
								});
							}
			});
			
			//radio options:
				/*
			$('.radioByDate input:radio').change(
				function(){
					$('.radioByItems input:radio').each(function(i){
						this.checked = false;
					});
					$('.radioOption').each(function(i){
						$(this).removeClass("radioOptionSelected");
					});
					$('.radioByDate').each(function(i){
						$(this).addClass("radioOptionSelected");
					});
					$('.bulkAddByItemsPanel').hide();
					$('.bulkAddByDatePanel').show();
					resizeFrame('grow');
				}
			);
			$('.radioByItems input:radio').change(
				function(){
					$('.radioByDate input:radio').each(function(i){
						this.checked = false;
					});
					$('.radioOption').each(function(i){
						$(this).removeClass("radioOptionSelected");
					});
					$('.radioByItems').each(function(i){
						$(this).addClass("radioOptionSelected");
					});
					$('.bulkAddByItemsPanel').show();
					$('.bulkAddByDatePanel').hide();
				}
			);
			$('.bulkAddByItemsPanel').hide();
			if($('.radioByDate input:radio').is(':checked')){
				//date option is selected... we need to setup the UI
				//this can happen if a user gets a warning message when
				//setting up the dates options
				$('.radioByDate input:radio').each(function(){
					$('.radioByItems input:radio').each(function(i){
						this.checked = false;
					});i
					$('.radioOption').each(function(i){
						$(this).removeClass("radioOptionSelected");
					});
					$('.radioByDate').each(function(i){
						$(this).addClass("radioOptionSelected");
					});
					$('.bulkAddByItemsPanel').hide();
					$('.bulkAddByDatePanel').show();
				});
			}*/
		});

	</script>
			<h:form id="syllabusEdit" styleClass="form-horizontal">
				<%@ include file="mainMenu.jsp" %>
				<h:outputText value="#{SyllabusTool.alertMessage}" styleClass="sak-banner-error" rendered="#{SyllabusTool.alertMessage != null}" />

				<div class="page-header">
					<h1><h:outputText value="#{msgs.add_sylla_bulk}"/></h1>
				</div>

				<div class="sak-banner-info">
					<h:outputText value="#{msgs.newSyllabusBulkForm}"/>
				</div>
					<h:panelGroup layout="block" styleClass="form-group">
						<h:outputLabel for="title" styleClass="control-label col-sm-2 col-lg-1">
							<h:outputText value="#{msgs.syllabus_title}"/>
						</h:outputLabel>
						<div class="col-sm-10 col-lg-11">
							<h:inputText styleClass="form-control" value="#{SyllabusTool.bulkEntry.title}" id="title"/>
						</div>
					</h:panelGroup>
					<h:panelGroup layout="block" styleClass="form-group">
						<h:panelGroup layout="block" styleClass="col-sm-offset-2 col-lg-offset-">
							<h:selectOneRadio id="radioByItems" value="#{SyllabusTool.bulkEntry.addByItems}" styleClass="radioByItems radioOption radioOptionSelected">
								<f:selectItem id="byItems" itemLabel="#{msgs.bulkAddByItems}" itemValue="1" />
							</h:selectOneRadio>
							<h:selectOneRadio id="radioByDate" value="#{SyllabusTool.bulkEntry.addByDate}" styleClass="radioByDate radioOption ">
								<f:selectItem id="byDate" itemLabel="#{msgs.bulkAddByDate}" itemValue="1" />
							</h:selectOneRadio>
						</h:panelGroup>
					</h:panelGroup>

					<!-- Add X Bulk Entries -->
					<h:panelGroup layout="block" styleClass="bulkAddByItemsPanel">
						<h:panelGroup layout="block" styleClass="form-group">
							<h:outputLabel for="numOfItems" styleClass="control-label col-sm-2 col-lg-1">
								<h:outputText value="#{msgs.numberOfItems}"/>
							</h:outputLabel>
							<div class="col-sm-10 col-lg-11">
								<h:inputText value="#{SyllabusTool.bulkEntry.bulkItems}" styleClass="form-control" id="numOfItems" size="3" maxlength="3"/>
							</div>
						</h:panelGroup>
					</h:panelGroup>

					<!-- Add Bulk Entries by date -->
					<h:panelGroup layout="block" styleClass="bulkAddByDatePanel">
						<h:panelGroup layout="block" styleClass="form-group">
							<h:outputLabel for="dataStartDate" styleClass="control-label col-sm-2 col-lg-1">
								<h:outputText value="#{msgs.startdatetitle}"/>
							</h:outputLabel>
							<div class="col-sm-10 col-lg-11">
								<h:inputText styleClass="datInputStart" value="#{SyllabusTool.bulkEntry.startDateString}" id="dataStartDate"/>
							</div>
						</h:panelGroup>
						<h:panelGroup layout="block" styleClass="form-group">
							<h:outputLabel for="dataEndDate" styleClass="control-label col-sm-2 col-lg-1">
								<h:outputText value="#{msgs.enddatetitle}"/>
							</h:outputLabel>
							<div class="col-sm-10 col-lg-11">
								<h:inputText styleClass="datInputEnd" value="#{SyllabusTool.bulkEntry.endDateString}" id="dataEndDate"/>
							</div>
						</h:panelGroup>
						<h:panelGroup layout="block" styleClass="form-group">
							<h:outputLabel for="dataStartTime" styleClass="control-label col-sm-2 col-lg-1">
								<h:outputText value="#{msgs.starttimetitle}"/>
							</h:outputLabel>
							<div class="col-sm-10 col-lg-11">
								<h:inputText styleClass="timeInput timeInputStart" value="#{SyllabusTool.bulkEntry.startTimeString}" id="dataStartTime"/>
								<f:verbatim><span class="fa fa-calendar-times-o" onclick="$('.timeInputStart').focus();"></span></f:verbatim>
							</div>
						</h:panelGroup>
						
						<h:panelGroup layout="block" styleClass="form-group">
							<h:outputLabel for="dataEndTime" styleClass="control-label col-sm-2 col-lg-1">
								<h:outputText value="#{msgs.endtimetitle}"/>
							</h:outputLabel>
							<div class="col-sm-10 col-lg-11">
								<h:inputText styleClass="timeInput timeInputEnd" value="#{SyllabusTool.bulkEntry.endTimeString}" id="dataEndTime"/>
								<f:verbatim><span class="fa fa-calendar-times-o" onclick="$('.timeInputEnd').focus();"></span></f:verbatim>
							</div>
						</h:panelGroup>

						<h:panelGroup layout="block" styleClass="form-group" rendered="#{SyllabusTool.calendarExistsForSite}">
							<div class="checkbox col-sm-offset-2 col-sm-10 col-lg-offset-1 col-lg-11">
								<h:outputLabel for="linkCalendar">
									<h:selectBooleanCheckbox id="linkCalendar" value="#{SyllabusTool.bulkEntry.linkCalendar}" />
									<h:outputText value="#{msgs.linkcalendartitle}"/>
								</h:outputLabel>
							</div>
						</h:panelGroup>
						<h:panelGroup layout="block" styleClass="form-group">
							<fieldset>
								<legend class="col-sm-2 col-lg-1 control-label">
								<h:outputLabel for="monday">
									<h:outputText value="#{msgs.classMeetingDays}"/>
								</h:outputLabel>
								</legend>
								<ul class="col-sm-10 col-lg-11">
									<li class="checkbox">
										<h:outputLabel>
											<h:selectBooleanCheckbox id="monday" value="#{SyllabusTool.bulkEntry.monday}" />
										<h:outputText value="#{msgs.monday}"/>
										</h:outputLabel>
									</li>
									<li class="checkbox">
										<h:outputLabel>
											<h:selectBooleanCheckbox id="tuesday" value="#{SyllabusTool.bulkEntry.tuesday}" />
											<h:outputText value="#{msgs.tuesday}"/>
										</h:outputLabel>
									</li>
									<li class="checkbox">
										<h:outputLabel>
											<h:selectBooleanCheckbox id="wednesday" value="#{SyllabusTool.bulkEntry.wednesday}" />
											<h:outputText value="#{msgs.wednesday}"/>
										</h:outputLabel>
									</li>
									<li class="checkbox">
										<h:outputLabel>
											<h:selectBooleanCheckbox id="thursday" value="#{SyllabusTool.bulkEntry.thursday}" />
											<h:outputText value="#{msgs.thursday}"/>
										</h:outputLabel>
									</li>
									<li class="checkbox">
										<h:outputLabel>
											<h:selectBooleanCheckbox id="friday" value="#{SyllabusTool.bulkEntry.friday}" />
											<h:outputText value="#{msgs.friday}"/>
										</h:outputLabel>
									</li>
									<li class="checkbox">
										<h:outputLabel>
											<h:selectBooleanCheckbox id="saturday" value="#{SyllabusTool.bulkEntry.saturday}" />
											<h:outputText value="#{msgs.saturday}"/>
										</h:outputLabel>
									</li>
									<li class="checkbox">
										<h:outputLabel>
											<h:selectBooleanCheckbox id="sunday" value="#{SyllabusTool.bulkEntry.sunday}" />
											<h:outputText value="#{msgs.sunday}"/>
										</h:outputLabel>
										
									</li>
								</ul>
							</fieldset>
						</h:panelGroup>
					</h:panelGroup>
						
				<sakai:button_bar>
					<h:commandButton
						action="#{SyllabusTool.processEditBulkPost}"
						styleClass="active"
						value="#{msgs.bar_publish}" 
						accesskey="s"
						title="#{msgs.button_publish}" />
					<h:commandButton
						action="#{SyllabusTool.processEditBulkDraft}"
						value="#{msgs.bar_new}" 
						accesskey="s"
						title="#{msgs.button_save}" />
					<h:commandButton
						action="#{SyllabusTool.processEditBulkCancel}"
						value="#{msgs.cancel}" 
						accesskey="x"
						title="#{msgs.button_cancel}" />
				</sakai:button_bar>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>

</f:view>
