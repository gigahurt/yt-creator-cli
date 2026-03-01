package net.gigahurt.ytcreatorcli.auth;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class YouTubeAuthRetryAspect {

    private final OAuth2Service authService;

    public YouTubeAuthRetryAspect(OAuth2Service authService) {
        this.authService = authService;
    }

    @Around("execution(* net.gigahurt.ytcreatorcli.service.*.*(..))")
    public Object retryOn400(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 400) {
                System.err.println("YouTube API returned 400 — re-authenticating and retrying...");
                authService.reauthorize();
                return pjp.proceed();
            }
            throw e;
        }
    }
}
