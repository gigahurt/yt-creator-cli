package net.gigahurt.ytcreatorcli.service;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoUploadServiceTest {

    @Mock YouTube youtube;
    @Mock YouTube.Videos mockVideos;
    @Mock YouTube.Videos.Insert mockInsert;
    @Mock YouTube.Videos.Delete mockDelete;
    @Mock YouTube.Videos.Rate mockRate;
    @Mock YouTube.Videos.GetRating mockGetRating;
    @Mock MediaHttpUploader mockUploader;

    private VideoUploadService service;

    @BeforeEach
    void setUp() {
        service = new VideoUploadService(youtube);
    }

    // ── uploadVideo ───────────────────────────────────────────────────────────

    @Test
    void uploadVideo_throwsWhenFileNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.uploadVideo("/no/such/file.mp4", "Title", null,
                        null, null, null, null, null));
    }

    @Test
    void uploadVideo_buildsMp4MimeType_forMp4Extension(@TempDir Path tempDir) throws Exception {
        File videoFile = tempDir.resolve("video.mp4").toFile();
        videoFile.createNewFile();

        ArgumentCaptor<FileContent> mediaCaptor = ArgumentCaptor.forClass(FileContent.class);
        Video uploaded = new Video().setId("v1");

        when(youtube.videos()).thenReturn(mockVideos);
        when(mockVideos.insert(anyList(), any(Video.class), mediaCaptor.capture()))
                .thenReturn(mockInsert);
        when(mockInsert.getMediaHttpUploader()).thenReturn(mockUploader);
        when(mockInsert.execute()).thenReturn(uploaded);

        service.uploadVideo(videoFile.getAbsolutePath(), "Title", null, null, null, null, null, null);

        assertEquals("video/mp4", mediaCaptor.getValue().getType());
    }

    @Test
    void uploadVideo_setsDefaultPrivate_whenVisibilityNull(@TempDir Path tempDir) throws Exception {
        File videoFile = tempDir.resolve("video.mp4").toFile();
        videoFile.createNewFile();

        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        Video uploaded = new Video().setId("v1");

        when(youtube.videos()).thenReturn(mockVideos);
        when(mockVideos.insert(anyList(), videoCaptor.capture(), any(FileContent.class)))
                .thenReturn(mockInsert);
        when(mockInsert.getMediaHttpUploader()).thenReturn(mockUploader);
        when(mockInsert.execute()).thenReturn(uploaded);

        service.uploadVideo(videoFile.getAbsolutePath(), "Title", null, null, null, null, null, null);

        assertEquals("private", videoCaptor.getValue().getStatus().getPrivacyStatus());
    }

    @Test
    void uploadVideo_setsLanguage_whenProvided(@TempDir Path tempDir) throws Exception {
        File videoFile = tempDir.resolve("video.mp4").toFile();
        videoFile.createNewFile();

        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        Video uploaded = new Video().setId("v1");

        when(youtube.videos()).thenReturn(mockVideos);
        when(mockVideos.insert(anyList(), videoCaptor.capture(), any(FileContent.class)))
                .thenReturn(mockInsert);
        when(mockInsert.getMediaHttpUploader()).thenReturn(mockUploader);
        when(mockInsert.execute()).thenReturn(uploaded);

        service.uploadVideo(videoFile.getAbsolutePath(), "Title", null, null, null, "public", null, "fr");

        VideoSnippet snippet = videoCaptor.getValue().getSnippet();
        assertEquals("fr", snippet.getDefaultLanguage());
        assertEquals("fr", snippet.getDefaultAudioLanguage());
    }

    // ── deleteVideo ───────────────────────────────────────────────────────────

    @Test
    void deleteVideo_callsApiDelete() throws IOException {
        when(youtube.videos()).thenReturn(mockVideos);
        when(mockVideos.delete(any())).thenReturn(mockDelete);

        service.deleteVideo("v1");

        verify(mockVideos).delete("v1");
        verify(mockDelete).execute();
    }

    // ── getVideoRating ────────────────────────────────────────────────────────

    @Test
    void getVideoRating_returnsNone_whenItemsEmpty() throws IOException {
        VideoGetRatingResponse response = new VideoGetRatingResponse().setItems(List.of());

        when(youtube.videos()).thenReturn(mockVideos);
        when(mockVideos.getRating(anyList())).thenReturn(mockGetRating);
        when(mockGetRating.execute()).thenReturn(response);

        assertEquals("none", service.getVideoRating("v1"));
    }

    @Test
    void getVideoRating_returnsRating_whenPresent() throws IOException {
        VideoRating rating = new VideoRating().setRating("like");
        VideoGetRatingResponse response = new VideoGetRatingResponse().setItems(List.of(rating));

        when(youtube.videos()).thenReturn(mockVideos);
        when(mockVideos.getRating(anyList())).thenReturn(mockGetRating);
        when(mockGetRating.execute()).thenReturn(response);

        assertEquals("like", service.getVideoRating("v1"));
    }

    // ── determineMimeType (via upload path) ───────────────────────────────────

    @Test
    void uploadVideo_usesWebmMimeType_forWebmFile(@TempDir Path tempDir) throws Exception {
        File videoFile = tempDir.resolve("video.webm").toFile();
        videoFile.createNewFile();

        ArgumentCaptor<FileContent> mediaCaptor = ArgumentCaptor.forClass(FileContent.class);
        Video uploaded = new Video().setId("v1");

        when(youtube.videos()).thenReturn(mockVideos);
        when(mockVideos.insert(anyList(), any(Video.class), mediaCaptor.capture()))
                .thenReturn(mockInsert);
        when(mockInsert.getMediaHttpUploader()).thenReturn(mockUploader);
        when(mockInsert.execute()).thenReturn(uploaded);

        service.uploadVideo(videoFile.getAbsolutePath(), "Title", null, null, null, null, null, null);

        assertEquals("video/webm", mediaCaptor.getValue().getType());
    }
}
