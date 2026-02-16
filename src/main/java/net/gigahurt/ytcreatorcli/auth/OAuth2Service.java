package net.gigahurt.ytcreatorcli.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import net.gigahurt.ytcreatorcli.config.AppProperties;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.util.List;

@Service
public class OAuth2Service {

    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/youtube",
            "https://www.googleapis.com/auth/youtube.force-ssl",
            "https://www.googleapis.com/auth/youtube.upload"
    );

    private final AppProperties props;
    private final NetHttpTransport httpTransport;
    private final GsonFactory jsonFactory;
    private Credential cachedCredential;

    public OAuth2Service(AppProperties props, NetHttpTransport httpTransport, GsonFactory jsonFactory) {
        this.props = props;
        this.httpTransport = httpTransport;
        this.jsonFactory = jsonFactory;
    }

    public synchronized Credential authorize() throws Exception {
        if (cachedCredential != null
                && (cachedCredential.getExpiresInSeconds() == null
                    || cachedCredential.getExpiresInSeconds() > 60)) {
            return cachedCredential;
        }

        GoogleClientSecrets secrets = GoogleClientSecrets.load(
                jsonFactory, new FileReader(props.clientSecretsPath()));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, secrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(props.tokensDirectory())))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();

        cachedCredential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        return cachedCredential;
    }
}
