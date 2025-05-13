package backend.academy.scrapper.service;

import static backend.academy.scrapper.test.util.TestUtil.generateChat;
import static backend.academy.scrapper.test.util.TestUtil.generateLink;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.repository.orm.ChatRepository;
import backend.academy.scrapper.repository.orm.FilterRepository;
import backend.academy.scrapper.repository.orm.LinkRepository;
import backend.academy.scrapper.repository.orm.SubscriptionRepository;
import backend.academy.scrapper.repository.orm.TagRepository;
import backend.academy.scrapper.repository.orm.entity.Chat;
import backend.academy.scrapper.repository.orm.entity.Link;
import backend.academy.scrapper.repository.orm.entity.Subscription;
import backend.academy.scrapper.repository.orm.entity.Tag;
import backend.academy.scrapper.service.orm.OrmLinksService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrmLinksServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private FilterRepository filterRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private OrmLinksService linksService;

    @Test
    @DisplayName("Add link")
    void notPresentLink() {
        Chat chat = generateChat(1L, "https://dot.com", "https://another.com");
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(tagRepository.findByName("tag23")).thenReturn(Optional.of(new Tag("tag23")));
        Link link = generateLink("https://third.com", 1);
        when(linkRepository.findByUrl("https://third.com")).thenReturn(Optional.of(link));
        when(subscriptionRepository.findByChatAndLink(any(), any()))
                .thenReturn(Optional.of(new Subscription(chat, link)));
        LinkResponse response = linksService.addLink(
                1L,
                AddLinkRequest.builder()
                        .link("https://third.com")
                        .filters(Collections.emptyList())
                        .tags(List.of("tag23"))
                        .build());
        assertEquals("https://third.com", response.url());
        assertEquals(List.of("tag23"), response.tags());
    }

    @Test
    @DisplayName("Remove link from absent chat")
    void removeLinkFromAbsentChat() {
        when(chatRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> linksService.removeLink(1L, new RemoveLinkRequest("https://another.com")));
        assertThatThrownBy(() -> linksService.removeLink(1L, new RemoveLinkRequest("https://another.com")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Не существует чата: 1");

        verifyNoInteractions(linkRepository);
    }

    @Test
    @DisplayName("Remove absent link")
    void removePresentLink() {
        when(chatRepository.findById(1L))
                .thenReturn(Optional.of(generateChat(1L, "https://dot.com", "https://another.com")));

        assertThatThrownBy(() -> linksService.removeLink(1L, new RemoveLinkRequest("https://third.com")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Не существует ссылки: https://third.com");
    }
}
