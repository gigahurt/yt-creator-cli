package net.gigahurt.ytcreatorcli.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppPropertiesTest {

    @Test
    void defaults_applied_when_all_nulls() {
        AppProperties props = new AppProperties(null, null, null, null);

        assertEquals("client_secrets.json", props.clientSecretsPath());
        assertEquals(System.getProperty("user.home") + "/.yt-creator-cli/tokens", props.tokensDirectory());
        assertEquals("yt-creator-cli", props.applicationName());
        assertNull(props.channelId());
    }

    @Test
    void defaults_applied_when_all_blank() {
        AppProperties props = new AppProperties("  ", "  ", "  ", null);

        assertEquals("client_secrets.json", props.clientSecretsPath());
        assertEquals(System.getProperty("user.home") + "/.yt-creator-cli/tokens", props.tokensDirectory());
        assertEquals("yt-creator-cli", props.applicationName());
    }

    @Test
    void explicit_values_are_kept() {
        AppProperties props = new AppProperties(
                "/path/to/secrets.json",
                "/tmp/tokens",
                "my-app",
                "UCtest123"
        );

        assertEquals("/path/to/secrets.json", props.clientSecretsPath());
        assertEquals("/tmp/tokens", props.tokensDirectory());
        assertEquals("my-app", props.applicationName());
        assertEquals("UCtest123", props.channelId());
    }

    @Test
    void default_clientSecretsPath_only() {
        AppProperties props = new AppProperties(null, "/tokens", "app", "ch1");

        assertEquals("client_secrets.json", props.clientSecretsPath());
        assertEquals("/tokens", props.tokensDirectory());
    }

    @Test
    void default_applicationName_only() {
        AppProperties props = new AppProperties("/secrets.json", "/tokens", null, "ch1");

        assertEquals("/secrets.json", props.clientSecretsPath());
        assertEquals("yt-creator-cli", props.applicationName());
    }
}
