package backend.academy.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.FilterRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.SubscriptionRepository;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.repository.entity.Chat;
import backend.academy.scrapper.repository.entity.Filter;
import backend.academy.scrapper.repository.entity.Link;
import backend.academy.scrapper.repository.entity.Subscription;
import backend.academy.scrapper.repository.entity.Tag;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class ScrapperApplicationTests {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private FilterRepository filterRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void setUp() {
        linkRepository.deleteAll();
        chatRepository.deleteAll();
        subscriptionRepository.deleteAll();
        tagRepository.deleteAll();
        filterRepository.deleteAll();
    }

    @Test
    @Order(1)
    void contextLoads() {}

    @Test
    @DisplayName("Create new chat")
    void createNewChat() {
        assertThat(chatRepository.count()).isZero();

        List<ResponseEntity<Void>> responses = IntStream.rangeClosed(1, 3)
                .mapToObj("/tg-chat/%s"::formatted)
                .map(s -> testRestTemplate.postForEntity(s, null, Void.class))
                .toList();

        assertThat(responses).map(ResponseEntity::getStatusCode).allMatch(HttpStatusCode::is2xxSuccessful);

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
                            });
        });
    }

    @Test
    @DisplayName("Delete chat")
    void deleteChat() {
        chatRepository.saveAndFlush(new Chat(1L));
        chatRepository.saveAndFlush(new Chat(2L));

        ResponseEntity<Void> response =
                testRestTemplate.exchange("/tg-chat/1", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(chatRepository.findAll()).hasSize(1).singleElement().satisfies(chat -> assertThat(chat.id())
                .isEqualTo(2L));
    }

    @Test
    @DisplayName("Delete chat cascade")
    void deleteChatCascade() {
        addLinkRequest("https://github.com/onekram/game", 1L, "t1", "f1");
        addLinkRequest("https://github.com/onekram/nogame", 1L, "t1", "f2");

        ResponseEntity<Void> response =
                testRestTemplate.exchange("/tg-chat/1", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        transactionTemplate.executeWithoutResult(ignored -> {
            assertThat(chatRepository.findAll()).isEmpty();
            assertThat(linkRepository.findAll()).isEmpty();
            assertThat(subscriptionRepository.findAll()).isEmpty();
            assertThat(tagRepository.findAll())
                    .singleElement()
                    .extracting(Tag::name)
                    .isEqualTo("t1");
            assertThat(filterRepository.findAll())
                    .hasSize(2)
                    .extracting(Filter::name)
                    .containsExactlyInAnyOrder("f1", "f2");
        });
    }

    @Test
    @DisplayName("Delete chat mutual links")
    void deleteChatMutualLinks() {
        addLinkRequest("https://github.com/onekram/game", 1L, "t1", "f1");
        addLinkRequest("https://github.com/onekram/game", 2L, "t1", "f2");

        ResponseEntity<Void> response =
                testRestTemplate.exchange("/tg-chat/1", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        transactionTemplate.executeWithoutResult(ignored -> {
            assertThat(chatRepository.findAll()).singleElement().satisfies(chat -> {
                assertThat(chat.id()).isEqualTo(2L);
                assertThat(chat.subscriptions())
                        .extracting(Subscription::link)
                        .extracting(Link::url)
                        .singleElement()
                        .isEqualTo("https://github.com/onekram/game");
            });
            assertThat(linkRepository.findAll())
                    .singleElement()
                    .extracting(Link::url)
                    .isEqualTo("https://github.com/onekram/game");
            assertThat(subscriptionRepository.findAll()).singleElement().satisfies(subscription -> {
                assertThat(subscription.chat().id()).isEqualTo(2L);
                assertThat(subscription.link().url()).isEqualTo("https://github.com/onekram/game");
            });
            assertThat(tagRepository.findAll())
                    .singleElement()
                    .extracting(Tag::name)
                    .isEqualTo("t1");
            assertThat(filterRepository.findAll())
                    .hasSize(2)
                    .extracting(Filter::name)
                    .containsExactlyInAnyOrder("f1", "f2");
        });
    }

    @Test
    @DisplayName("Get all chat links")
    void listLink() {
        saveChatWithTrackedUrls(1L, "https://www.google.com", "https://www.github.com");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", "1");
        headers.set("Content-Type", "application/json");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ListLinksResponse> response =
                testRestTemplate.exchange("/links", HttpMethod.GET, requestEntity, ListLinksResponse.class);

        assertThat(response).satisfies(r -> {
            assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(r.getBody()).isNotNull();
            assertThat(r.getBody().links()).hasSize(2);
            assertThat(r.getBody().size()).isEqualTo(2);
            assertThat(r.getBody().links())
                    .extracting(LinkResponse::url)
                    .containsExactlyInAnyOrder("https://www.google.com", "https://www.github.com");
        });
    }

    @Test
    @DisplayName("Add link")
    void addLink() {
        saveChatWithTrackedUrls(1L, "https://www.google.com", "https://www.github.com");

        addLinkRequest("https://third.com/onekram/game", 1L);

        transactionTemplate.executeWithoutResult((ignored) -> {
            assertThat(chatRepository.findAll()).singleElement().satisfies(c -> {
                assertThat(c.id()).isEqualTo(1L);
                assertThat(c.subscriptions())
                        .satisfiesExactlyInAnyOrder(
                                s -> {
                                    assertThat(s.link().url()).isEqualTo("https://third.com/onekram/game");
                                    assertThat(s.chat().id()).isEqualTo(1L);
                                    assertThat(s.tags())
                                            .isNotNull()
                                            .extracting(Tag::name)
                                            .containsExactly("tag1");
                                    assertThat(s.filters())
                                            .isNotNull()
                                            .extracting(Filter::name)
                                            .containsExactly("filter1");
                                },
                                s -> {
                                    assertThat(s.link().url()).isEqualTo("https://www.google.com");
                                    assertThat(s.chat().id()).isEqualTo(1L);
                                    assertThat(s.tags()).isEmpty();
                                    assertThat(s.filters()).isEmpty();
                                },
                                s -> {
                                    assertThat(s.link().url()).isEqualTo("https://www.github.com");
                                    assertThat(s.chat().id()).isEqualTo(1L);
                                    assertThat(s.tags()).isEmpty();
                                    assertThat(s.filters()).isEmpty();
                                });
            });

            assertThat(linkRepository.findAll())
                    .hasSize(3)
                    .extracting(Link::url)
                    .containsExactlyInAnyOrder(
                            "https://third.com/onekram/game", "https://www.google.com", "https://www.github.com");

            assertThat(tagRepository.findAll()).singleElement().satisfies(tag -> {
                assertThat(tag.name()).isEqualTo("tag1");
                assertThat(tag.subscriptions())
                        .singleElement()
                        .extracting(Subscription::link)
                        .extracting(Link::url)
                        .isEqualTo("https://third.com/onekram/game");
            });

            assertThat(filterRepository.findAll()).singleElement().satisfies(filter -> {
                assertThat(filter.name()).isEqualTo("filter1");
                assertThat(filter.subscriptions())
                        .singleElement()
                        .extracting(Subscription::link)
                        .extracting(Link::url)
                        .isEqualTo("https://third.com/onekram/game");
            });
        });

        addLinkRequest("https://third.com/onekram/game", 2L);
        transactionTemplate.executeWithoutResult((ignored) -> {
            assertThat(chatRepository.findAll())
                    .hasSize(2)
                    .satisfiesExactlyInAnyOrder(
                            c -> {
                                assertThat(c.id()).isEqualTo(1L);
                                assertThat(c.subscriptions())
                                        .extracting(Subscription::link)
                                        .extracting(Link::url)
                                        .containsExactlyInAnyOrder(
                                                "https://third.com/onekram/game",
                                                "https://www.google.com",
                                                "https://www.github.com");
                            },
                            c -> {
                                assertThat(c.id()).isEqualTo(2L);
                                assertThat(c.subscriptions())
                                        .extracting(Subscription::link)
                                        .extracting(Link::url)
                                        .containsExactlyInAnyOrder("https://third.com/onekram/game");
                            });

            assertThat(linkRepository.findAll())
                    .hasSize(3)
                    .extracting(Link::url)
                    .containsExactlyInAnyOrder(
                            "https://third.com/onekram/game", "https://www.google.com", "https://www.github.com");

            assertThat(tagRepository.findAll()).singleElement().satisfies(tag -> {
                assertThat(tag.name()).isEqualTo("tag1");
                assertThat(tag.subscriptions())
                        .hasSize(2)
                        .extracting(Subscription::link)
                        .extracting(Link::url)
                        .containsOnly("https://third.com/onekram/game");
            });

            assertThat(filterRepository.findAll()).singleElement().satisfies(filter -> {
                assertThat(filter.name()).isEqualTo("filter1");
                assertThat(filter.subscriptions())
                        .hasSize(2)
                        .extracting(Subscription::link)
                        .extracting(Link::url)
                        .containsOnly("https://third.com/onekram/game");
            });
        });
    }

    @Test
    @DisplayName("Delete link")
    void deleteLink() {
        saveChatWithTrackedUrls(1L, "https://www.google.com", "https://www.github.com");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", "1");
        headers.set("Content-Type", "application/json");
        RemoveLinkRequest addLinkRequest = new RemoveLinkRequest("https://www.github.com");
        HttpEntity<RemoveLinkRequest> requestEntity = new HttpEntity<>(addLinkRequest, headers);

        ResponseEntity<LinkResponse> response =
                testRestTemplate.exchange("/links", HttpMethod.DELETE, requestEntity, LinkResponse.class);

        assertThat(response).satisfies(r -> {
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

            assertThat(subscriptionRepository.findAll())
                    .singleElement()
                    .extracting(Subscription::link)
                    .extracting(Link::url)
                    .isEqualTo("https://www.google.com");
        });
    }

    @Nested
    @DisplayName("Test for scheduling jobs")
    class Scheduling {
        @Test
        void scheduleRequestToGithubSingleChat() {
            addLinkRequest("https://github.com/onekram/game", 1L);
            await().atMost(5, TimeUnit.SECONDS)
                    .pollInterval(1, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(moreThanOrExactly(1), getRequestedFor(urlMatching("/repos/onekram/game/issues.*")));
                        verify(
                                moreThanOrExactly(1),
                                postRequestedFor(urlMatching("/updates"))
                                        .withRequestBody(matchingJsonPath(
                                                "$.resourceUrl", equalTo("https://github.com/onekram/game")))
                                        .withRequestBody(matchingJsonPath("$.tgChatIds", containing("1"))));
                    });
        }

        @Test
        void scheduleRequestToGithubTwoChats() {
            addLinkRequest("https://github.com/onekram/game", 1L);
            addLinkRequest("https://github.com/onekram/game", 2L);
            await().atMost(5, TimeUnit.SECONDS)
                    .pollInterval(1, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(moreThanOrExactly(1), getRequestedFor(urlMatching("/repos/onekram/game/issues.*")));
                        verify(
                                moreThanOrExactly(1),
                                postRequestedFor(urlMatching("/updates"))
                                        .withRequestBody(matchingJsonPath(
                                                "$.resourceUrl", equalTo("https://github.com/onekram/game")))
                                        .withRequestBody(matchingJsonPath("$.tgChatIds", containing("1")))
                                        .withRequestBody(matchingJsonPath("$.tgChatIds", containing("2"))));
                    });
        }

        @Test
        void scheduleRequestToGithubTwoChatsTwoLinks() {
            addLinkRequest("https://github.com/onekram/game", 1L);
            addLinkRequest("https://github.com/onekram/game", 2L);
            addLinkRequest("https://github.com/oleg/tbank", 2L);
            await().atMost(5, TimeUnit.SECONDS)
                    .pollInterval(1, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(moreThanOrExactly(1), getRequestedFor(urlMatching("/repos/onekram/game/issues.*")));
                        verify(moreThanOrExactly(1), getRequestedFor(urlMatching("/repos/oleg/tbank/issues.*")));
                        verify(
                                moreThanOrExactly(1),
                                postRequestedFor(urlMatching("/updates"))
                                        .withRequestBody(matchingJsonPath(
                                                "$.resourceUrl", equalTo("https://github.com/onekram/game")))
                                        .withRequestBody(matchingJsonPath("$.tgChatIds", containing("1")))
                                        .withRequestBody(matchingJsonPath("$.tgChatIds", containing("2"))));
                        verify(
                                moreThanOrExactly(1),
                                postRequestedFor(urlMatching("/updates"))
                                        .withRequestBody(matchingJsonPath(
                                                "$.resourceUrl", equalTo("https://github.com/oleg/tbank")))
                                        .withRequestBody(matchingJsonPath("$.tgChatIds", containing("2"))));
                    });
        }
    }

    private void addLinkRequest(String url, Long tgChatId) {
        addLinkRequest(url, tgChatId, "tag1", "filter1");
    }

    private void addLinkRequest(String url, Long tgChatId, String tag, String filter) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", tgChatId.toString());
        headers.set("Content-Type", "application/json");
        AddLinkRequest addLinkRequest = new AddLinkRequest(url, List.of(tag), List.of(filter));
        HttpEntity<AddLinkRequest> requestEntity = new HttpEntity<>(addLinkRequest, headers);

        ResponseEntity<LinkResponse> response =
                testRestTemplate.exchange("/links", HttpMethod.POST, requestEntity, LinkResponse.class);

        assertThat(response).satisfies(r -> {
            assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(r.getBody()).isNotNull();
            assertThat(r.getBody().url()).isEqualTo(url);
            assertThat(r.getBody().tags()).containsExactly(tag);
            assertThat(r.getBody().filters()).containsExactly(filter);
        });
    }

    private void saveChatWithTrackedUrls(long id, String... urls) {
        transactionTemplate.executeWithoutResult((ignored) -> {
            Chat chat = chatRepository.save(new Chat(id));
            Arrays.stream(urls)
                    .map(url -> linkRepository.save(new Link(url)))
                    .map(link -> subscriptionRepository.save(new Subscription(chat, link)))
                    .forEach(chat.subscriptions()::add);
        });
    }
}
