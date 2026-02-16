package net.gigahurt.ytcreatorcli.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;

import net.gigahurt.ytcreatorcli.auth.OAuth2Service;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class YouTubeConfig {

    @Bean
    public GsonFactory gsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    @Bean
    public NetHttpTransport netHttpTransport() {
        return new NetHttpTransport();
    }

    @Bean
    public YouTube youTube(AppProperties props, OAuth2Service authService,
                           NetHttpTransport transport, GsonFactory jsonFactory) throws Exception {
        return new YouTube.Builder(transport, jsonFactory, authService.authorize())
                .setApplicationName(props.applicationName())
                .build();
    }
}
