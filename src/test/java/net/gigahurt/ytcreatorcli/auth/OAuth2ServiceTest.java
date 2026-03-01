package net.gigahurt.ytcreatorcli.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import net.gigahurt.ytcreatorcli.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2ServiceTest {

    @Mock AppProperties props;
    @Mock Credential mockCredential;

    private OAuth2Service service;

    @BeforeEach
    void setUp() {
        service = new OAuth2Service(props, new NetHttpTransport(), GsonFactory.getDefaultInstance());
    }

    @Test
    void authorize_returnsCachedCredential_whenValidAndNotExpiringSoon() throws Exception {
        when(mockCredential.getExpiresInSeconds()).thenReturn(120L);
        injectCachedCredential(mockCredential);

        Credential result = service.authorize();

        assertSame(mockCredential, result);
    }

    @Test
    void authorize_returnsCachedCredential_whenExpiryIsNull() throws Exception {
        when(mockCredential.getExpiresInSeconds()).thenReturn(null);
        injectCachedCredential(mockCredential);

        Credential result = service.authorize();

        assertSame(mockCredential, result);
    }

    @Test
    void authorize_proceedsToReauth_whenCredentialExpiringSoon() throws Exception {
        when(mockCredential.getExpiresInSeconds()).thenReturn(30L);
        injectCachedCredential(mockCredential);
        when(props.clientSecretsPath()).thenReturn("/nonexistent/secrets.json");

        // Falls through to file load which fails — meaning it did NOT return the cached cred
        assertThrows(Exception.class, () -> service.authorize());
    }

    @Test
    void reauthorize_clearsCachedCredential_beforeReauth() throws Exception {
        // No need to stub getExpiresInSeconds — reauthorize() nulls the credential first,
        // so authorize() skips the expiry check entirely.
        injectCachedCredential(mockCredential);
        when(props.clientSecretsPath()).thenReturn("/nonexistent/secrets.json");

        // reauthorize sets cachedCredential=null, then authorize() throws on missing file
        assertThrows(Exception.class, () -> service.reauthorize());

        Field field = OAuth2Service.class.getDeclaredField("cachedCredential");
        field.setAccessible(true);
        assertNull(field.get(service));
    }

    private void injectCachedCredential(Credential credential) throws Exception {
        Field field = OAuth2Service.class.getDeclaredField("cachedCredential");
        field.setAccessible(true);
        field.set(service, credential);
    }
}
