package net.gigahurt.ytcreatorcli.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ytcli")
public record AppProperties(
    String clientSecretsPath,
    String tokensDirectory,
    String applicationName
) {
    public AppProperties {
        if (clientSecretsPath == null || clientSecretsPath.isBlank()) {
            clientSecretsPath = "client_secrets.json";
        }
        if (tokensDirectory == null || tokensDirectory.isBlank()) {
            tokensDirectory = System.getProperty("user.home") + "/.yt-creator-cli/tokens";
        }
        if (applicationName == null || applicationName.isBlank()) {
            applicationName = "yt-creator-cli";
        }
    }
}
