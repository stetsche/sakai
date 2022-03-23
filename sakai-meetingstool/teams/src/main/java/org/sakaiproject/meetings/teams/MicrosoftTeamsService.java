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

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.gson.JsonPrimitive;
import com.microsoft.graph.models.AadUserConversationMember;
import com.microsoft.graph.models.ConversationMember;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.GroupSetting;
import com.microsoft.graph.models.SettingValue;
import com.microsoft.graph.models.Team;

@Slf4j
@Component(value="MicrosoftTeamsService")
public class MicrosoftTeamsService  {

    private GraphServiceClient graphClient;
    
    
    public MicrosoftTeamsService () {
    	AdminAuthProvider authRober = new AdminAuthProvider();
        graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(authRober)
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

}
