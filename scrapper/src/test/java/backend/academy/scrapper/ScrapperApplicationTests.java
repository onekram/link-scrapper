package backend.academy.scrapper;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.FilterRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.repository.entity.Chat;
import backend.academy.scrapper.repository.entity.Filter;
import backend.academy.scrapper.repository.entity.Link;
import backend.academy.scrapper.repository.entity.Tag;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScrapperApplicationTests {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private FilterRepository filterRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void setUp() {
        linkRepository.deleteAll();
        chatRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    @DisplayName("Create new chat")
    void createNewChat() {
        assertThat(chatRepository.count()).isZero();

        List<ResponseEntity<Void>> responses = IntStream.rangeClosed(1, 3)
            .mapToObj("/tg-chat/%s"::formatted)
            .map(s -> testRestTemplate.postForEntity(s, null, Void.class))
            .toList();

        assertThat(responses)
            .map(ResponseEntity::getStatusCode)
            .allMatch(HttpStatusCode::is2xxSuccessful);

        transactionTemplate.executeWithoutResult(ignored -> {
            Instant fiveSecondsAgo = Instant.now().minusSeconds(5);
            assertThat(chatRepository.findAll())
                .satisfiesExactlyInAnyOrder(
                    chat -> {
                        assertThat(chat.id()).isEqualTo(1L);
                        assertThat(chat.createdAt()).isAfterOrEqualTo(fiveSecondsAgo);
                    },
                    chat -> {
                        assertThat(chat.id()).isEqualTo(2L);
                        assertThat(chat.createdAt()).isAfterOrEqualTo(fiveSecondsAgo);
                    },
                    chat -> {
                        assertThat(chat.id()).isEqualTo(3L);
                        assertThat(chat.createdAt()).isAfterOrEqualTo(fiveSecondsAgo);
                    }
                );
        });
    }

    @Test
    @DisplayName("Delete chat")
    void deleteChat() {
        chatRepository.saveAndFlush(new Chat(1L));
        chatRepository.saveAndFlush(new Chat(2L));

        ResponseEntity<Void> response = testRestTemplate
            .exchange("/tg-chat/1", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(chatRepository.findAll())
            .hasSize(1)
            .singleElement()
            .satisfies(chat -> assertThat(chat.id()).isEqualTo(2L));
    }

    @Test
    @DisplayName("Get all chat links")
    void listLink() {
        Chat chat = chatRepository.save(new Chat(1L));
        Link first = linkRepository.save(new Link("https://www.google.com", Collections.emptySet(), Collections.emptySet()));
        Link second = linkRepository.save(new Link("https://www.github.com", Collections.emptySet(), Collections.emptySet()));

        chat.links().add(first);
        chat.links().add(second);
        first.chats().add(chat);
        second.chats().add(chat);
        chatRepository.save(chat);
        linkRepository.save(first);
        linkRepository.save(second);

        chatRepository.flush();
        linkRepository.flush();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", "1");
        headers.set("Content-Type", "application/json");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ListLinksResponse> response = testRestTemplate.exchange(
            "/links",
            HttpMethod.GET,
            requestEntity,
            ListLinksResponse.class
        );

        assertThat(response)
            .satisfies(r -> {
                assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(r.getBody()).isNotNull();
                assertThat(r.getBody().links()).hasSize(2);
                assertThat(r.getBody().size()).isEqualTo(2);
                assertThat(r.getBody().links()).extracting(LinkResponse::url)
                    .containsExactlyInAnyOrder("https://www.google.com", "https://www.github.com");
            });
    }

    @Test
    @DisplayName("Add link")
    void addLink() {
        Chat chat = chatRepository.save(new Chat(1L));
        Link first = linkRepository.save(new Link("https://www.google.com", Collections.emptySet(), Collections.emptySet()));
        Link second = linkRepository.save(new Link("https://www.github.com", Collections.emptySet(), Collections.emptySet()));

        chat.links().add(first);
        chat.links().add(second);
        first.chats().add(chat);
        second.chats().add(chat);
        chatRepository.save(chat);
        linkRepository.save(first);
        linkRepository.save(second);

        chatRepository.flush();
        linkRepository.flush();

        addLinkRequest("https://third.com/onekram/game", 1L);

        transactionTemplate.executeWithoutResult((ignored) -> {
            assertThat(chatRepository.findAll())
                .singleElement()
                .satisfies(c -> {
                    assertThat(c.id()).isEqualTo(1L);
                    assertThat(c.links())
                        .extracting(Link::url)
                        .containsExactlyInAnyOrder("https://third.com/onekram/game", "https://www.google.com", "https://www.github.com");
                });

            assertThat(linkRepository.findAll())
                .satisfiesOnlyOnce(link -> {
                    assertThat(link.url()).isEqualTo("https://third.com/onekram/game");
                    assertThat(link.chats()).extracting(Chat::id).containsExactly(1L);
                    assertThat(link.tags()).map(Tag::name).containsExactly("tag1");
                    assertThat(link.filters()).map(Filter::name).containsExactly("filter1");
                });

            assertThat(tagRepository.findAll())
                .singleElement()
                .satisfies(filter -> {
                    assertThat(filter.name()).isEqualTo("tag1");
                    assertThat(filter.links())
                        .extracting(Link::url)
                        .containsExactly("https://third.com/onekram/game");
                });

            assertThat(filterRepository.findAll())
                .singleElement()
                .satisfies(filter -> {
                    assertThat(filter.name()).isEqualTo("filter1");
                    assertThat(filter.links())
                        .extracting(Link::url)
                        .containsExactly("https://third.com/onekram/game");
                });
        });

        addLinkRequest("https://third.com/onekram/game", 2L);
        transactionTemplate.executeWithoutResult((ignored) -> {
            assertThat(chatRepository.findAll())
                .hasSize(2)
                .satisfiesExactlyInAnyOrder(
                    c -> {
                        assertThat(c.id()).isEqualTo(1L);
                        assertThat(c.links())
                            .extracting(Link::url)
                            .containsExactlyInAnyOrder("https://third.com/onekram/game", "https://www.google.com", "https://www.github.com");
                    }, c -> {
                        assertThat(c.id()).isEqualTo(2L);
                        assertThat(c.links())
                            .extracting(Link::url)
                            .containsExactlyInAnyOrder("https://third.com/onekram/game");
                    }
                );

            assertThat(linkRepository.findAll())
                .satisfiesOnlyOnce(link -> {
                    assertThat(link.url()).isEqualTo("https://third.com/onekram/game");
                    assertThat(link.chats()).extracting(Chat::id).containsExactlyInAnyOrder(2L);
                    assertThat(link.tags()).map(Tag::name).containsExactly("tag1");
                    assertThat(link.filters()).map(Filter::name).containsExactly("filter1");
                });

            assertThat(tagRepository.findAll())
                .singleElement()
                .satisfies(filter -> {
                    assertThat(filter.name()).isEqualTo("tag1");
                    assertThat(filter.links())
                        .extracting(Link::url)
                        .containsExactly("https://third.com/onekram/game", "https://third.com/onekram/game");
                });

            assertThat(filterRepository.findAll())
                .singleElement()
                .satisfies(filter -> {
                    assertThat(filter.name()).isEqualTo("filter1");
                    assertThat(filter.links())
                        .extracting(Link::url)
                        .containsExactly("https://third.com/onekram/game", "https://third.com/onekram/game");
                });
        });
    }

    @Test
    @DisplayName("Delete link")
    void deleteLink() {
        Chat chat = chatRepository.save(new Chat(1L));
        Link first = linkRepository.save(new Link("https://www.google.com", Collections.emptySet(), Collections.emptySet()));
        Link second = linkRepository.save(new Link("https://www.github.com", Collections.emptySet(), Collections.emptySet()));

        chat.links().add(first);
        chat.links().add(second);
        first.chats().add(chat);
        second.chats().add(chat);
        chatRepository.save(chat);
        linkRepository.save(first);
        linkRepository.save(second);

        chatRepository.flush();
        linkRepository.flush();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", "1");
        headers.set("Content-Type", "application/json");
        RemoveLinkRequest addLinkRequest = new RemoveLinkRequest("https://www.github.com");
        HttpEntity<RemoveLinkRequest> requestEntity = new HttpEntity<>(addLinkRequest, headers);

        ResponseEntity<LinkResponse> response = testRestTemplate.exchange(
            "/links",
            HttpMethod.DELETE,
            requestEntity,
            LinkResponse.class
        );

        assertThat(response)
            .satisfies(r -> {
                assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(r.getBody()).isNotNull();
                assertThat(r.getBody().url()).isEqualTo("https://www.github.com");
            });

        transactionTemplate.executeWithoutResult((ignored) -> {
            assertThat(chatRepository.findAll())
                .singleElement()
                .extracting(Chat::id)
                .isEqualTo(1L);

            assertThat(linkRepository.findAll())
                .singleElement()
                .extracting(Link::url)
                .isEqualTo("https://www.google.com");
        });
    }

    @Test
    @DisplayName("Scheduling request to github")
    void scheduleRequestToGithub() {
        addLinkRequest("https://github.com/onekram/game", 1L);
        await()
            .atMost(2, TimeUnit.SECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                verify(1, getRequestedFor(urlMatching("/repos/onekram/game")));
                verify(1, postRequestedFor(urlMatching("/updates"))
                    .withRequestBody(matchingJsonPath("$.url", equalTo("https://github.com/onekram/game")))
                    .withRequestBody(matchingJsonPath("$.tgChatIds", containing("1"))));
            });
        WireMock.reset();

        addLinkRequest("https://github.com/onekram/game", 2L);
        await()
            .atMost(2, TimeUnit.SECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                verify(2, getRequestedFor(urlMatching("/repos/onekram/game")));
                verify(1, postRequestedFor(urlMatching("/updates"))
                    .withRequestBody(matchingJsonPath("$.url", equalTo("https://github.com/onekram/game")))
                    .withRequestBody(matchingJsonPath("$.tgChatIds", containing("1"))));
                verify(1, postRequestedFor(urlMatching("/updates"))
                    .withRequestBody(matchingJsonPath("$.url", equalTo("https://github.com/onekram/game")))
                    .withRequestBody(matchingJsonPath("$.tgChatIds", containing("2"))));
            });
    }

    private void addLinkRequest(String url, Long tgChatId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", tgChatId.toString());
        headers.set("Content-Type", "application/json");
        AddLinkRequest addLinkRequest = new AddLinkRequest(url,
            List.of("tag1"),
            List.of("filter1"));
        HttpEntity<AddLinkRequest> requestEntity = new HttpEntity<>(addLinkRequest, headers);

        ResponseEntity<LinkResponse> response = testRestTemplate.exchange(
            "/links",
            HttpMethod.POST,
            requestEntity,
            LinkResponse.class
        );

        assertThat(response)
            .satisfies(r -> {
                assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(r.getBody()).isNotNull();
                assertThat(r.getBody().url()).isEqualTo(url);
                assertThat(r.getBody().tags()).containsExactly("tag1");
                assertThat(r.getBody().filters()).containsExactly("filter1");
            });
    }
}
