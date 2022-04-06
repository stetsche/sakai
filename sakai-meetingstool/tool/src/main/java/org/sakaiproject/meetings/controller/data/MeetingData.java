package org.sakaiproject.meetings.controller.data;

import java.io.Serializable;

import lombok.Data;

@Data
public class MeetingData implements Serializable {

	private static final long serialVersionUID = 3284276542110972341L;

	private String id;
	private String title;
	private String description;
	private String siteId;
	private String startDate;
	private String endDate;
	private String ownerId;
	private boolean saveToCalendar;
	private NotificationType notificationType;
	private Integer live; 
	
}
