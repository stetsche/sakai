// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package org.sakaiproject.meetings.teams;

import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.ConversationMemberCollectionPage;
import com.microsoft.graph.requests.ConversationMemberCollectionResponse;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.GroupCollectionPage;
import com.microsoft.graph.requests.GroupCollectionRequestBuilder;
import com.microsoft.graph.requests.UserCollectionPage;
import com.microsoft.graph.requests.UserCollectionRequestBuilder;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.meetings.teams.data.TeamsMeetingData;

import com.google.gson.JsonPrimitive;
import com.microsoft.graph.models.AadUserConversationMember;
import com.microsoft.graph.models.ConversationMember;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.GroupSetting;
import com.microsoft.graph.models.Identity;
import com.microsoft.graph.models.IdentitySet;
import com.microsoft.graph.models.LobbyBypassScope;
import com.microsoft.graph.models.LobbyBypassSettings;
import com.microsoft.graph.models.MeetingParticipantInfo;
import com.microsoft.graph.models.MeetingParticipants;
import com.microsoft.graph.models.OnlineMeeting;
import com.microsoft.graph.models.OnlineMeetingPresenters;
import com.microsoft.graph.models.OnlineMeetingRole;
import com.microsoft.graph.models.SettingValue;
import com.microsoft.graph.models.Team;

@Slf4j
public class MicrosoftTeamsService  {

    private static final ServerConfigurationService serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
    
    private GraphServiceClient graphClient;
    
    private static final String MSTEAMS_PREFIX = "meetings.msteams.";
    private static final String AUTHORITY = "authority";
    private static final String CLIENT_ID = "clientId";
    private static final String SECRET = "secret";
    private static final String SCOPE = "scope";
    
    public MicrosoftTeamsService() {
        log.info("Initializing Microsoft Teams Service");
        String authority = serverConfigurationService.getString(MSTEAMS_PREFIX + AUTHORITY, null);
        String clientId = serverConfigurationService.getString(MSTEAMS_PREFIX + CLIENT_ID, null);
        String secret = serverConfigurationService.getString(MSTEAMS_PREFIX + SECRET, null);
        String scope = serverConfigurationService.getString(MSTEAMS_PREFIX + SCOPE, null);
        AdminAuthProvider authProvider = new AdminAuthProvider(authority, clientId, secret, scope);
        graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }
    
    public List<User> getAzureUserList() {
    	List<User> userList = new ArrayList<>();
		try {
			UserCollectionPage page = graphClient.users().buildRequest().get();
			while(page != null) {
				for(User o : page.getCurrentPage()) {
					System.out.println(o.id+": "+o.displayName);
					
				}
				userList.addAll(page.getCurrentPage());
				UserCollectionRequestBuilder builder = page.getNextPage();
				if(builder == null)break;
				page = builder.buildRequest().get();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return userList;
    }
    
    
    public List<Group> getGroups() {
    	List<Group> groupList = new ArrayList<>();
		try {
			GroupCollectionPage page = graphClient.groups().buildRequest().get();
			while(page != null) {
				for(Group o : page.getCurrentPage()) {
					System.out.println("Group:"+o.id+" -> "+o.displayName);
					if(o.settings != null) {
						for(GroupSetting gs : o.settings.getCurrentPage()) {
							System.out.println(">>>"+gs.displayName);
							for(SettingValue val : gs.values) {
								System.out.println("\t - "+val.name+" => "+val.value);
							}
						}
					}
				}
				groupList.addAll(page.getCurrentPage());
				GroupCollectionRequestBuilder builder = page.getNextPage();
				if(builder == null)break;
				page = builder.buildRequest().get();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return groupList;
    }
    
    
    public void createTeam() throws Exception {

        try {
    		Team team = new Team();
    		team.additionalDataManager().put("template@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/teamsTemplates('standard')"));
    		team.displayName = "My Sample Team";
    		team.description = "My Sample Team�s Description";
    		LinkedList<ConversationMember> membersList = new LinkedList<ConversationMember>();
    		AadUserConversationMember members = new AadUserConversationMember();
    		LinkedList<String> rolesList = new LinkedList<String>();
    		rolesList.add("owner");
    		members.roles = rolesList;
    		members.oDataType = "#microsoft.graph.aadUserConversationMember";
    		members.additionalDataManager().put("user@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/users('89557e16-62b9-4988-982b-2fb652464aa1')"));
    		membersList.add(members);
    		ConversationMemberCollectionResponse conversationMemberCollectionResponse = new ConversationMemberCollectionResponse();
    		conversationMemberCollectionResponse.value = membersList;
    		ConversationMemberCollectionPage conversationMemberCollectionPage = new ConversationMemberCollectionPage(conversationMemberCollectionResponse, null);
    		team.members = conversationMemberCollectionPage;

    		graphClient.teams()
    		    .buildRequest()
    		    .post(team);
    		
        } catch(Exception ex){
            System.out.println("Oops! We have an exception of type - " + ex.getClass());
            System.out.println("Exception message - " + ex.getMessage());
            throw ex;
        }
    }
    
    
    public User getUserByMicrosoftLogin(String microsoftLogin) {
    	User result = graphClient.users(microsoftLogin).buildRequest().get();
    	return result;
    }
    
    
    
    public TeamsMeetingData onlineMeeting(String presenter, String subject, Instant startDate, Instant endDate) throws ParseException {
    	
    	// Get presenter user 
    	User organizerUser = getUserByMicrosoftLogin(presenter);
    	
    	// Organizer
    	MeetingParticipantInfo organizer = new MeetingParticipantInfo();
    	IdentitySet organizerIdentity = new IdentitySet();
    	Identity iden = new Identity();
    	iden.id = organizerUser.id;
    	iden.displayName = organizerUser.displayName;
    	organizerIdentity.application = iden; 
    	organizer.identity = organizerIdentity;
    	organizer.role = OnlineMeetingRole.PRESENTER;
    	
    	// Participants
    	MeetingParticipants participants = new MeetingParticipants();
    	participants.organizer = organizer;
    	
    	// Lobby Settings
    	LobbyBypassSettings lobbySettings = new LobbyBypassSettings();
    	lobbySettings.scope = LobbyBypassScope.ORGANIZATION;
    	
    	// Online Meeting
        OnlineMeeting onlineMeeting = new OnlineMeeting();
        if (startDate != null) onlineMeeting.startDateTime = OffsetDateTime.ofInstant(startDate, ZoneId.systemDefault());
        if (endDate != null) onlineMeeting.endDateTime = OffsetDateTime.ofInstant(endDate, ZoneId.systemDefault());
    	onlineMeeting.participants = participants;
    	onlineMeeting.subject = subject;
    	onlineMeeting.lobbyBypassSettings = lobbySettings;
    	onlineMeeting.allowedPresenters = OnlineMeetingPresenters.ROLE_IS_PRESENTER;
    	
    	OnlineMeeting meeting = graphClient.users(organizerUser.id).onlineMeetings()
    	    .buildRequest()
    	    .post(onlineMeeting);

    	TeamsMeetingData result = new TeamsMeetingData();
    	result.setId(meeting.id);
    	result.setJoinUrl(meeting.joinWebUrl);
    	return result;
    }
    
    public void deleteMeeting(String organizerUser, String meetingId) {
    	User user = getUserByMicrosoftLogin(organizerUser);
    	graphClient.users(user.id).onlineMeetings(meetingId).buildRequest().delete();
    }

}
