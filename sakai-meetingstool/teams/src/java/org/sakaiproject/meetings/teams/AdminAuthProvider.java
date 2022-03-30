package org.sakaiproject.meetings.teams;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.graph.authentication.IAuthenticationProvider;

public class AdminAuthProvider implements IAuthenticationProvider {

    private static String authority;
    private static String clientId;
    private static String secret;
    private static String scope;
    private static ConfidentialClientApplication app;
    
	@Override
	public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
		CompletableFuture<String> token = new CompletableFuture<>();
		try {
System.out.println("getting authorization");
			readConfig();
System.out.println("config read!");
			BuildConfidentialClientObject();
System.out.println("Confidential client built");
			IAuthenticationResult result = getAccessTokenByClientCredentialGrant();
System.out.println("getAccessTokenByClientCredentialGrant executed!");
			token.complete(result.accessToken());
		} catch (Exception e) {
			System.out.println("------ Oops! We have an exception of type - " + e.getClass());
            System.out.println("------ Exception message - " + e.getMessage());
		}
		return token;
	}
	
	/**
	 * Build confidential client object
	 * @throws Exception
	 */
	private void BuildConfidentialClientObject() throws Exception {
    	app = ConfidentialClientApplication.builder(
                clientId,
                ClientCredentialFactory.createFromSecret(secret))
                .authority(authority)
                .build();		        
    }
	
	/**
	 * Get Token 
	 * @return
	 * @throws Exception
	 */
	private IAuthenticationResult getAccessTokenByClientCredentialGrant() throws Exception {
		
    	// With client credentials flows the scope is ALWAYS of the shape "resource/.default", as the
        // application permissions need to be set statically (in the portal), and then granted by a tenant administrator
        ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(
        		Collections.singleton(scope))
                .build();
    	
        CompletableFuture<IAuthenticationResult> future = app.acquireToken(clientCredentialParam);
        return future.get();
    }
	
    
	/**
	 * Read app connection properties
	 * @throws IOException
	 */
    private void readConfig() throws IOException {
System.out.println("Gonna read properties!");
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("graph.properties"));
        authority = properties.getProperty("AUTHORITY");
        clientId = properties.getProperty("CLIENT_ID");
        secret = properties.getProperty("SECRET");
        scope = properties.getProperty("SCOPE");
System.out.println("Properties read! Example: "+clientId);
    }
	
}