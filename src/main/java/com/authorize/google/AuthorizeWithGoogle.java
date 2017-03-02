package com.authorize.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by satyad on 02/03/17.
 */
public class AuthorizeWithGoogle {
    /**
     * The following are the pre-requisites to use Google apis
     */
    private static JsonFactory JSON_FACTORY;
    private static GoogleClientSecrets clientSecrets;
    private static HttpTransport HTTP_TRANSPORT;
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    //general scopes : "profile email";
    private static final String SCOPE = "profile";
    // URI to exchange the token with
    private static final String TOKEN_URI = "https://accounts.google.com/o/oauth2/token";
    // URI to get auth token
    private static final String AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    // client ID that is obtained after creating application in console.developers.google.com
    private static final String clientId = "709132143330-mbe71p4dcikvldtt6t549jnvhqg0sp0d.apps.googleusercontent.com";
    // client secret that is obtained after creating application in console.developers.google.com
    private static final String clientSecret = "X2oXgQ3mrlBK8M_RlJjFFAjf";
    // application name that is configured while creating application in console.developers.google.com
    private static final String APPLICATION_NAME = "oauth-basic-client-app";

    // path to store credentials { on successful authorization we are storing the creds just for testing }
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/plus_sample");
    private static Plus plus;

    static {
        try {
            clientSecrets = new GoogleClientSecrets();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            JSON_FACTORY = JacksonFactory.getDefaultInstance();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /*
      * This method forms google client secrets and initiates auth flow.. it prompts the user to open the url in browser or opens it
      * On consent screen, user has to authorize or un-authorize
     */
    private static Credential authorizeWithGoogle() throws IOException {

        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);
        details.setTokenUri(TOKEN_URI);
        details.setAuthUri(AUTH_URI);
        details.setRedirectUris(Collections.singletonList("http://localhost"));
        clientSecrets.setWeb(details);

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singleton(SCOPE))
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    public static void main(String[] args) {
        try {
            Credential credential = authorizeWithGoogle();

            plus = new Plus.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(
                    APPLICATION_NAME).build();
            getProfile();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the profile for the authenticated user.
     */
    private static void getProfile() throws IOException {
        System.out.println("Get my Google+ profile");

        Person profile = plus.people().get("me").execute();
        show(profile);
    }

    static void show(Person person) {
        System.out.println("id: " + person.getId());
        System.out.println("name: " + person.getDisplayName());
        System.out.println("image url: " + person.getImage().getUrl());
        System.out.println("profile url: " + person.getUrl());
    }
}
