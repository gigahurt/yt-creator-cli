package net.gigahurt.ytcreatorcli.service;

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
class ThumbnailServiceTest {

    @Mock YouTube youtube;
    @Mock YouTube.Thumbnails mockThumbnails;
    @Mock YouTube.Thumbnails.Set mockThumbSet;

    private ThumbnailService service;

    @BeforeEach
    void setUp() {
        service = new ThumbnailService(youtube);
    }

    @Test
    void setThumbnail_throwsWhenFileNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.setThumbnail("v1", "/nonexistent/thumb.png"));
    }

    @Test
    void setThumbnail_usesPngMimeType_forPngFile(@TempDir Path tempDir) throws Exception {
        File pngFile = tempDir.resolve("thumb.png").toFile();
        pngFile.createNewFile();

        ArgumentCaptor<FileContent> mediaCaptor = ArgumentCaptor.forClass(FileContent.class);
        ThumbnailSetResponse response = new ThumbnailSetResponse().setItems(List.of());

        when(youtube.thumbnails()).thenReturn(mockThumbnails);
        when(mockThumbnails.set(eq("v1"), mediaCaptor.capture())).thenReturn(mockThumbSet);
        when(mockThumbSet.execute()).thenReturn(response);

        service.setThumbnail("v1", pngFile.getAbsolutePath());

        assertEquals("image/png", mediaCaptor.getValue().getType());
    }

    @Test
    void setThumbnail_usesJpegMimeType_forJpgFile(@TempDir Path tempDir) throws Exception {
        File jpgFile = tempDir.resolve("thumb.jpg").toFile();
        jpgFile.createNewFile();

        ArgumentCaptor<FileContent> mediaCaptor = ArgumentCaptor.forClass(FileContent.class);
        ThumbnailSetResponse response = new ThumbnailSetResponse().setItems(List.of());

        when(youtube.thumbnails()).thenReturn(mockThumbnails);
        when(mockThumbnails.set(eq("v1"), mediaCaptor.capture())).thenReturn(mockThumbSet);
        when(mockThumbSet.execute()).thenReturn(response);

        service.setThumbnail("v1", jpgFile.getAbsolutePath());

        assertEquals("image/jpeg", mediaCaptor.getValue().getType());
    }

    @Test
    void setThumbnail_returnsFallbackMessage_whenNoThumbnailUrl(@TempDir Path tempDir) throws Exception {
        File pngFile = tempDir.resolve("thumb.png").toFile();
        pngFile.createNewFile();

        ThumbnailSetResponse response = new ThumbnailSetResponse().setItems(List.of());

        when(youtube.thumbnails()).thenReturn(mockThumbnails);
        when(mockThumbnails.set(any(), any())).thenReturn(mockThumbSet);
        when(mockThumbSet.execute()).thenReturn(response);

        String result = service.setThumbnail("v1", pngFile.getAbsolutePath());

        assertEquals("Thumbnail set successfully", result);
    }

    @Test
    void setThumbnail_returnsUrl_whenResponseContainsDefaultThumbnail(@TempDir Path tempDir)
            throws Exception {
        File pngFile = tempDir.resolve("thumb.png").toFile();
        pngFile.createNewFile();

        Thumbnail defaultThumb = new Thumbnail().setUrl("https://example.com/thumb.png");
        ThumbnailDetails details = new ThumbnailDetails().setDefault(defaultThumb);
        ThumbnailSetResponse response = new ThumbnailSetResponse().setItems(List.of(details));

        when(youtube.thumbnails()).thenReturn(mockThumbnails);
        when(mockThumbnails.set(any(), any())).thenReturn(mockThumbSet);
        when(mockThumbSet.execute()).thenReturn(response);

        String result = service.setThumbnail("v1", pngFile.getAbsolutePath());

        assertEquals("https://example.com/thumb.png", result);
    }
}
