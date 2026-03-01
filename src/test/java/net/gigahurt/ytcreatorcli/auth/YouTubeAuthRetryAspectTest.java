package net.gigahurt.ytcreatorcli.auth;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YouTubeAuthRetryAspectTest {

    @Mock
    private OAuth2Service authService;

    @Mock
    private ProceedingJoinPoint pjp;

    private YouTubeAuthRetryAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new YouTubeAuthRetryAspect(authService);
    }

    @Test
    void retryOn400_returnsResult_whenNoException() throws Throwable {
        when(pjp.proceed()).thenReturn("success");

        Object result = aspect.retryOn400(pjp);

        assertEquals("success", result);
        verify(pjp, times(1)).proceed();
        verifyNoInteractions(authService);
    }

    @Test
    void retryOn400_reauthorizesAndRetries_on400Error() throws Throwable {
        GoogleJsonResponseException ex400 = make400Exception();
        when(pjp.proceed())
                .thenThrow(ex400)
                .thenReturn("retried");

        Object result = aspect.retryOn400(pjp);

        assertEquals("retried", result);
        verify(authService).reauthorize();
        verify(pjp, times(2)).proceed();
    }

    @Test
    void retryOn400_rethrows_onNon400Error() throws Throwable {
        GoogleJsonResponseException ex403 = makeJsonResponseException(403);
        when(pjp.proceed()).thenThrow(ex403);

        GoogleJsonResponseException thrown = assertThrows(
                GoogleJsonResponseException.class,
                () -> aspect.retryOn400(pjp)
        );

        assertEquals(403, thrown.getStatusCode());
        verifyNoInteractions(authService);
        verify(pjp, times(1)).proceed();
    }

    // --- helpers ---

    private GoogleJsonResponseException make400Exception() {
        return makeJsonResponseException(400);
    }

    private GoogleJsonResponseException makeJsonResponseException(int statusCode) {
        HttpResponseException.Builder builder = new HttpResponseException.Builder(
                statusCode, "Error", new HttpHeaders());
        return new GoogleJsonResponseException(builder, new GoogleJsonError());
    }
}
