package net.gigahurt.ytcreatorcli.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock YouTube youtube;
    @Mock YouTube.CommentThreads mockThreads;
    @Mock YouTube.CommentThreads.List mockThreadList;
    @Mock YouTube.CommentThreads.Insert mockThreadInsert;
    @Mock YouTube.Comments mockComments;
    @Mock YouTube.Comments.List mockCommentList;
    @Mock YouTube.Comments.Insert mockCommentInsert;
    @Mock YouTube.Comments.Update mockCommentUpdate;
    @Mock YouTube.Comments.Delete mockCommentDelete;
    @Mock YouTube.Comments.SetModerationStatus mockSetModeration;

    private CommentService service;

    @BeforeEach
    void setUp() {
        service = new CommentService(youtube);
    }

    // ── listCommentThreads ────────────────────────────────────────────────────

    @Test
    void listCommentThreads_defaultsOrderToTime_whenNull() throws IOException {
        CommentThreadListResponse response = new CommentThreadListResponse().setItems(List.of());

        when(youtube.commentThreads()).thenReturn(mockThreads);
        when(mockThreads.list(anyList())).thenReturn(mockThreadList);
        when(mockThreadList.setVideoId(any())).thenReturn(mockThreadList);
        when(mockThreadList.setOrder(any())).thenReturn(mockThreadList);
        when(mockThreadList.setMaxResults(anyLong())).thenReturn(mockThreadList);
        when(mockThreadList.execute()).thenReturn(response);

        service.listCommentThreads("v1", null, 20, null);

        verify(mockThreadList).setOrder("time");
    }

    @Test
    void listCommentThreads_setsPageToken_whenProvided() throws IOException {
        CommentThreadListResponse response = new CommentThreadListResponse().setItems(List.of());

        when(youtube.commentThreads()).thenReturn(mockThreads);
        when(mockThreads.list(anyList())).thenReturn(mockThreadList);
        when(mockThreadList.setVideoId(any())).thenReturn(mockThreadList);
        when(mockThreadList.setOrder(any())).thenReturn(mockThreadList);
        when(mockThreadList.setMaxResults(anyLong())).thenReturn(mockThreadList);
        when(mockThreadList.setPageToken(any())).thenReturn(mockThreadList);
        when(mockThreadList.execute()).thenReturn(response);

        service.listCommentThreads("v1", "relevance", 20, "tok123");

        verify(mockThreadList).setPageToken("tok123");
        verify(mockThreadList).setOrder("relevance");
    }

    // ── insertCommentThread ───────────────────────────────────────────────────

    @Test
    void insertCommentThread_buildsCorrectStructure() throws IOException {
        ArgumentCaptor<CommentThread> captor = ArgumentCaptor.forClass(CommentThread.class);
        CommentThread thread = new CommentThread();

        when(youtube.commentThreads()).thenReturn(mockThreads);
        when(mockThreads.insert(anyList(), captor.capture())).thenReturn(mockThreadInsert);
        when(mockThreadInsert.execute()).thenReturn(thread);

        service.insertCommentThread("v1", "Hello world");

        CommentThread captured = captor.getValue();
        assertEquals("v1", captured.getSnippet().getVideoId());
        assertEquals("Hello world",
                captured.getSnippet().getTopLevelComment().getSnippet().getTextOriginal());
    }

    // ── insertReply ───────────────────────────────────────────────────────────

    @Test
    void insertReply_setsParentId() throws IOException {
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        Comment comment = new Comment();

        when(youtube.comments()).thenReturn(mockComments);
        when(mockComments.insert(anyList(), captor.capture())).thenReturn(mockCommentInsert);
        when(mockCommentInsert.execute()).thenReturn(comment);

        service.insertReply("thread1", "My reply");

        CommentSnippet snippet = captor.getValue().getSnippet();
        assertEquals("thread1", snippet.getParentId());
        assertEquals("My reply", snippet.getTextOriginal());
    }

    // ── updateComment ─────────────────────────────────────────────────────────

    @Test
    void updateComment_setsNewText() throws IOException {
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        Comment comment = new Comment();

        when(youtube.comments()).thenReturn(mockComments);
        when(mockComments.update(anyList(), captor.capture())).thenReturn(mockCommentUpdate);
        when(mockCommentUpdate.execute()).thenReturn(comment);

        service.updateComment("c1", "Updated text");

        Comment captured = captor.getValue();
        assertEquals("c1", captured.getId());
        assertEquals("Updated text", captured.getSnippet().getTextOriginal());
    }

    // ── deleteComment ─────────────────────────────────────────────────────────

    @Test
    void deleteComment_callsApiDelete() throws IOException {
        when(youtube.comments()).thenReturn(mockComments);
        when(mockComments.delete(any())).thenReturn(mockCommentDelete);

        service.deleteComment("c1");

        verify(mockComments).delete("c1");
        verify(mockCommentDelete).execute();
    }

    // ── setModerationStatus ───────────────────────────────────────────────────

    @Test
    void setModerationStatus_passesBanAuthorFlag() throws IOException {
        when(youtube.comments()).thenReturn(mockComments);
        when(mockComments.setModerationStatus(anyList(), any())).thenReturn(mockSetModeration);
        when(mockSetModeration.setBanAuthor(anyBoolean())).thenReturn(mockSetModeration);

        service.setModerationStatus(List.of("c1", "c2"), "rejected", true);

        verify(mockSetModeration).setBanAuthor(true);
        verify(mockSetModeration).execute();
    }
}
