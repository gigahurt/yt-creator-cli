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
class SubscriptionServiceTest {

    @Mock YouTube youtube;
    @Mock YouTube.Subscriptions mockSubscriptions;
    @Mock YouTube.Subscriptions.List mockSubList;
    @Mock YouTube.Subscriptions.Insert mockSubInsert;
    @Mock YouTube.Subscriptions.Delete mockSubDelete;

    private SubscriptionService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionService(youtube);
    }

    // ── listMySubscriptions ───────────────────────────────────────────────────

    @Test
    void listMySubscriptions_defaultsOrderToAlphabetical_whenNull() throws IOException {
        SubscriptionListResponse response = new SubscriptionListResponse().setItems(List.of());

        when(youtube.subscriptions()).thenReturn(mockSubscriptions);
        when(mockSubscriptions.list(anyList())).thenReturn(mockSubList);
        when(mockSubList.setMine(anyBoolean())).thenReturn(mockSubList);
        when(mockSubList.setMaxResults(anyLong())).thenReturn(mockSubList);
        when(mockSubList.setOrder(any())).thenReturn(mockSubList);
        when(mockSubList.execute()).thenReturn(response);

        service.listMySubscriptions(50, null, null);

        verify(mockSubList).setOrder("alphabetical");
    }

    @Test
    void listMySubscriptions_setsPageToken_whenProvided() throws IOException {
        SubscriptionListResponse response = new SubscriptionListResponse().setItems(List.of());

        when(youtube.subscriptions()).thenReturn(mockSubscriptions);
        when(mockSubscriptions.list(anyList())).thenReturn(mockSubList);
        when(mockSubList.setMine(anyBoolean())).thenReturn(mockSubList);
        when(mockSubList.setMaxResults(anyLong())).thenReturn(mockSubList);
        when(mockSubList.setOrder(any())).thenReturn(mockSubList);
        when(mockSubList.setPageToken(any())).thenReturn(mockSubList);
        when(mockSubList.execute()).thenReturn(response);

        service.listMySubscriptions(50, "pageToken1", "relevance");

        verify(mockSubList).setPageToken("pageToken1");
        verify(mockSubList).setOrder("relevance");
    }

    // ── subscribe ─────────────────────────────────────────────────────────────

    @Test
    void subscribe_buildsCorrectResourceId() throws IOException {
        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        Subscription created = new Subscription().setId("sub1");

        when(youtube.subscriptions()).thenReturn(mockSubscriptions);
        when(mockSubscriptions.insert(anyList(), captor.capture())).thenReturn(mockSubInsert);
        when(mockSubInsert.execute()).thenReturn(created);

        Subscription result = service.subscribe("UCchannel");

        Subscription captured = captor.getValue();
        ResourceId resourceId = captured.getSnippet().getResourceId();
        assertEquals("youtube#channel", resourceId.getKind());
        assertEquals("UCchannel", resourceId.getChannelId());
        assertSame(created, result);
    }

    // ── unsubscribe ───────────────────────────────────────────────────────────

    @Test
    void unsubscribe_callsApiDelete() throws IOException {
        when(youtube.subscriptions()).thenReturn(mockSubscriptions);
        when(mockSubscriptions.delete(any())).thenReturn(mockSubDelete);

        service.unsubscribe("sub1");

        verify(mockSubscriptions).delete("sub1");
        verify(mockSubDelete).execute();
    }

    // ── findSubscription ──────────────────────────────────────────────────────

    @Test
    void findSubscription_returnsId_whenFound() throws IOException {
        Subscription sub = new Subscription().setId("sub42");
        SubscriptionListResponse response = new SubscriptionListResponse().setItems(List.of(sub));

        when(youtube.subscriptions()).thenReturn(mockSubscriptions);
        when(mockSubscriptions.list(anyList())).thenReturn(mockSubList);
        when(mockSubList.setMine(anyBoolean())).thenReturn(mockSubList);
        when(mockSubList.setForChannelId(any())).thenReturn(mockSubList);
        when(mockSubList.execute()).thenReturn(response);

        String id = service.findSubscription("UCchannel");

        assertEquals("sub42", id);
    }

    @Test
    void findSubscription_returnsNull_whenNotFound() throws IOException {
        SubscriptionListResponse response = new SubscriptionListResponse().setItems(List.of());

        when(youtube.subscriptions()).thenReturn(mockSubscriptions);
        when(mockSubscriptions.list(anyList())).thenReturn(mockSubList);
        when(mockSubList.setMine(anyBoolean())).thenReturn(mockSubList);
        when(mockSubList.setForChannelId(any())).thenReturn(mockSubList);
        when(mockSubList.execute()).thenReturn(response);

        assertNull(service.findSubscription("UCmissing"));
    }
}
