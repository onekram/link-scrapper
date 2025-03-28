package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRecord;
import backend.academy.scrapper.repository.LinksRepository;
import backend.academy.scrapper.test.util.TestUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinksServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private LinksRepository linksRepository;

    @InjectMocks
    private LinksService linksService;

    @Test
    @DisplayName("Add not present link")
    void notPresentLink() {
        when(chatRepository.getLinks(1L)).thenReturn(List.of(42L, 43L));
        mockGetLink(42L, "https://dot.com");
        mockGetLink(43L, "https://another.com");
        when(linksRepository.addLink(any(LinkRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LinkResponse response = linksService.addLink(
                1L,
                new AddLinkRequest.Builder()
                        .link("https://third.com")
                        .tags(List.of("tag23"))
                        .build());
        assertThat(response.getId()).isNotIn(42L, 43L);
        assertEquals("https://third.com", response.getUrl());
        assertEquals(List.of("tag23"), response.getTags());

        ArgumentCaptor<LinkRecord> captor = ArgumentCaptor.forClass(LinkRecord.class);
        verify(linksRepository, times(1)).addLink(captor.capture());
        assertThat(captor.getValue().getId()).isNotIn(42L, 43L);
        verify(chatRepository, times(1)).addLink(1L, response.getId());
    }

    @Test
    @DisplayName("Add duplicate link")
    void duplicateLink() {
        when(chatRepository.getLinks(1L)).thenReturn(List.of(42L, 43L));
        mockGetLink(42L, "https://dot.com");
        when(linksRepository.getLink(43L))
                .thenReturn(TestUtil.generateLinkRecord(
                        43L, "https://another.com", Collections.emptyList(), List.of("oldFilter")));
        when(linksRepository.addLink(any(LinkRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LinkResponse response = linksService.addLink(
                1L,
                new AddLinkRequest.Builder()
                        .link("https://another.com")
                        .filters(List.of("newFilter"))
                        .build());

        assertEquals(43L, response.getId());
        assertEquals("https://another.com", response.getUrl());
        assertEquals(List.of("newFilter"), response.getFilters());

        ArgumentCaptor<LinkRecord> captor = ArgumentCaptor.forClass(LinkRecord.class);
        verify(linksRepository, times(1)).addLink(captor.capture());
        assertEquals(43L, captor.getValue().getId());
        verify(chatRepository, times(1)).addLink(1L, response.getId());
    }

    @Test
    @DisplayName("Remove present link")
    void removePresentLink() {
        when(chatRepository.getLinks(1L)).thenReturn(List.of(42L, 43L));
        mockGetLink(42L, "https://dot.com");
        when(linksRepository.getLink(43L))
                .thenReturn(TestUtil.generateLinkRecord(
                        43L, "https://another.com", List.of("oldTag"), Collections.emptyList()));

        LinkResponse response = linksService.removeLink(1L, new RemoveLinkRequest("https://another.com"));

        assertEquals(43L, response.getId());
        assertEquals("https://another.com", response.getUrl());
        assertEquals(List.of("oldTag"), response.getTags());

        verify(linksRepository, times(1)).removeLink(43L);
    }

    @Test
    @DisplayName("Fetch id and links by type")
    void fetchByType() {
        when(chatRepository.fetchAll()).thenReturn(List.of(1L, 2L));
        when(chatRepository.getLinks(1L)).thenReturn(List.of(42L, 43L));
        when(chatRepository.getLinks(2L)).thenReturn(List.of(44L, 45L));
        mockGetLink(42L, "https://dot.com", LinkType.GITHUB);
        mockGetLink(43L, "https://second.com", LinkType.GITHUB);
        mockGetLink(44L, "https://third.com", LinkType.STACK_OVERFLOW);
        mockGetLink(45L, "https://fourth.com", LinkType.GITHUB);

        Map<Long, List<LinkRecord>> result = linksService.fetchIdAndLinksByType(LinkType.GITHUB);

        assertEquals(
                result,
                Map.of(
                        1L,
                        List.of(
                                TestUtil.generateLinkRecord(42L, "https://dot.com", LinkType.GITHUB),
                                TestUtil.generateLinkRecord(43L, "https://second.com", LinkType.GITHUB)),
                        2L,
                        List.of(TestUtil.generateLinkRecord(45L, "https://fourth.com", LinkType.GITHUB))));
    }

    private void mockGetLink(long id, String url) {
        when(linksRepository.getLink(id)).thenReturn(TestUtil.generateLinkRecord(id, url));
    }

    private void mockGetLink(long id, String url, LinkType linkType) {
        when(linksRepository.getLink(id)).thenReturn(TestUtil.generateLinkRecord(id, url, linkType));
    }
}
