# yt-creator-cli

A command-line tool for managing YouTube video metadata via the YouTube Data API v3. Built with Java 21, Spring Boot 4, and Spring Shell.

## Prerequisites

- Java 21+
- Maven 3.9+
- A Google Cloud project with the YouTube Data API v3 enabled

## Google Cloud Setup

You need OAuth2 client credentials to authenticate with the YouTube API.

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select an existing one)
3. Navigate to **APIs & Services > Library**, search for **YouTube Data API v3**, and enable it
4. Navigate to **APIs & Services > Credentials**
5. Click **Create Credentials > OAuth client ID**
   - If prompted, configure the **OAuth consent screen** first (External is fine for personal use — add yourself as a test user)
   - Application type: **Desktop app**
   - Give it a name (e.g. `yt-creator-cli`)
6. Download the JSON file and save it as `client_secrets.json` in the project root

On first run, the CLI will open a browser window for you to authorize access. Tokens are cached in `~/.yt-creator-cli/tokens/` so you only need to do this once.

### Required OAuth Scopes

The CLI requests the following scopes:
- `youtube` — read/write access to your YouTube account
- `youtube.force-ssl` — required for video updates
- `youtube.upload` — required for thumbnail uploads

## Build

```bash
mvn package
```

## Usage

Run commands directly as CLI arguments:

```bash
java -jar target/yt-creator-cli-0.1.0-SNAPSHOT.jar <command> [options]
```

### Commands

#### search-videos

Search for public videos on a channel.

```bash
java -jar target/yt-creator-cli-0.1.0-SNAPSHOT.jar search-videos \
  --channel-id <CHANNEL_ID> \
  --query "search terms" \
  --max-results 10
```

#### list-videos

List all videos on a channel (including private and unlisted).

```bash
java -jar target/yt-creator-cli-0.1.0-SNAPSHOT.jar list-videos \
  --channel-id <CHANNEL_ID> \
  --query "optional filter" \
  --max-results 50
```

#### get-video

Get current metadata for a video.

```bash
java -jar target/yt-creator-cli-0.1.0-SNAPSHOT.jar get-video \
  --video-id <VIDEO_ID>
```

#### update-video

Update video metadata. Only specified fields are changed; omitted fields are left as-is.

```bash
java -jar target/yt-creator-cli-0.1.0-SNAPSHOT.jar update-video \
  --video-id <VIDEO_ID> \
  --title "New Title" \
  --description "New description" \
  --tags "tag1, tag2, tag3" \
  --category-id 20 \
  --language en \
  --audio-language en \
  --visibility public \
  --made-for-kids false \
  --publish-at 2026-03-01T15:00:00Z \
  --thumbnail /path/to/thumbnail.png
```

The `--category-id` option accepts either a numeric ID or a name (e.g. `GAMING`, `ENTERTAINMENT`). Setting `--publish-at` automatically sets visibility to `private` as required by the API.

#### create-playlist

```bash
java -jar target/yt-creator-cli-0.1.0-SNAPSHOT.jar create-playlist \
  --title "My Playlist" \
  --description "Playlist description" \
  --visibility private
```

#### add-to-playlist

```bash
java -jar target/yt-creator-cli-0.1.0-SNAPSHOT.jar add-to-playlist \
  --playlist-id <PLAYLIST_ID> \
  --video-id <VIDEO_ID> \
  --position 0
```

### Video Categories

| ID | Category |
|----|----------|
| 1  | Film & Animation |
| 2  | Autos & Vehicles |
| 10 | Music |
| 15 | Pets & Animals |
| 17 | Sports |
| 19 | Travel & Events |
| 20 | Gaming |
| 22 | People & Blogs |
| 23 | Comedy |
| 24 | Entertainment |
| 25 | News & Politics |
| 26 | Howto & Style |
| 27 | Education |
| 28 | Science & Technology |
| 29 | Nonprofits & Activism |

## Configuration

Configuration can be set via `application.properties`, environment variables, or CLI args:

| Property | Env Var | Default |
|---|---|---|
| `ytcli.client-secrets-path` | `YTCLI_CLIENT_SECRETS_PATH` | `client_secrets.json` |
| `ytcli.tokens-directory` | `YTCLI_TOKENS_DIRECTORY` | `~/.yt-creator-cli/tokens` |
| `ytcli.application-name` | `YTCLI_APPLICATION_NAME` | `yt-creator-cli` |

## Shell Quoting

When passing values with spaces in non-interactive mode, wrap them in escaped quotes:

```bash
java -jar target/yt-creator-cli-0.1.0-SNAPSHOT.jar update-video \
  --video-id abc123 \
  --title "\"My Video Title\""
```

Video IDs starting with a dash also need quoting to avoid being parsed as flags:

```bash
--video-id "\"-zWHhaYFuRg\""
```
