package backend.academy.scrapper.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRecord;
import backend.academy.scrapper.repository.LinksRepository;
import backend.academy.scrapper.service.ChatService;
import backend.academy.scrapper.service.LinksService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LinksController.class)
@Import({ChatService.class, LinksService.class})
class LinksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatRepository chatRepository;

    @MockitoBean
    private LinksRepository linksRepository;

    @Test
    void getLinksOk() throws Exception {
        when(chatRepository.getLinks(1L)).thenReturn(List.of(42L, 43L));
        when(linksRepository.getLink(42L))
                .thenReturn(new LinkRecord(
                        42L,
                        new URI("https://dot.com"),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        LinkType.GITHUB));
        when(linksRepository.getLink(43L))
                .thenReturn(new LinkRecord(
                        43L,
                        new URI("https://another.com"),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        LinkType.STACK_OVERFLOW));
        mockMvc.perform(get("/links").contentType(MediaType.APPLICATION_JSON).header("Tg-Chat-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(2));

        verify(chatRepository, times(1)).getLinks(1L);
        verify(linksRepository, times(2)).getLink(anyLong());
    }

    @Test
    void getLinksNegaviveTgChatId() throws Exception {
        mockMvc.perform(get("/links").contentType(MediaType.APPLICATION_JSON).header("Tg-Chat-Id", -1L))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.exceptionName").value("BadRequestException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Невалидный идентификатор чата: -1"))
                .andExpect(jsonPath("$.stacktrace").isArray());

        verifyNoInteractions(chatRepository);
        verifyNoInteractions(linksRepository);
    }

    @Test
    void getLinksTypeMismatchTgChatId() throws Exception {
        mockMvc.perform(get("/links").contentType(MediaType.APPLICATION_JSON).header("Tg-Chat-Id", "chat-id"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentTypeMismatchException"))
                .andExpect(jsonPath("$.exceptionMessage").exists())
                .andExpect(jsonPath("$.stacktrace").isArray());

        verifyNoInteractions(chatRepository);
        verifyNoInteractions(linksRepository);
    }

    @Test
    void addLinkOk() throws Exception {
        when(chatRepository.getLinks(1L)).thenReturn(List.of(42L, 43L));
        when(linksRepository.getLink(42L))
                .thenReturn(new LinkRecord(
                        42L,
                        new URI("https://dot.com"),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        LinkType.GITHUB));
        when(linksRepository.getLink(43L))
                .thenReturn(new LinkRecord(
                        43L,
                        new URI("https://another.com"),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        LinkType.STACK_OVERFLOW));
        when(linksRepository.addLink(any()))
                .thenReturn(new LinkRecord(
                        123L, new URI("https://third.com"), Collections.emptyList(), Collections.emptyList(), null));
        mockMvc.perform(post("/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Tg-Chat-Id", 1L)
                        .content(objectMapper.writeValueAsString(new AddLinkRequest(
                                "https://third.com", Collections.emptyList(), Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123L))
                .andExpect(jsonPath("$.url").value("https://third.com"));

        verify(chatRepository, times(1)).getLinks(1L);
        verify(linksRepository, times(2)).getLink(anyLong());
    }

    @Test
    void addLinkWrongSchema() throws Exception {
        mockMvc.perform(
                        post("/links")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Tg-Chat-Id", 1L)
                                .content(
                                        """
                    {
                      "link": "https://third.com",
                      "badFilters": ["oldFilter"],
                      "wrongTags": ["tag"]
                    }
                    """))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.exceptionName").value("HttpMessageNotReadableException"))
                .andExpect(jsonPath("$.exceptionMessage").exists())
                .andExpect(jsonPath("$.stacktrace").isArray());

        verifyNoInteractions(chatRepository);
        verifyNoInteractions(linksRepository);
    }

    @Test
    void removeLinkOk() throws Exception {
        when(chatRepository.getLinks(1L)).thenReturn(List.of(42L, 43L));
        when(linksRepository.getLink(42L))
                .thenReturn(new LinkRecord(
                        42L,
                        new URI("https://dot.com"),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        LinkType.GITHUB));
        when(linksRepository.getLink(43L))
                .thenReturn(new LinkRecord(
                        43L,
                        new URI("https://another.com"),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        LinkType.STACK_OVERFLOW));
        mockMvc.perform(delete("/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Tg-Chat-Id", 1L)
                        .content(objectMapper.writeValueAsString(new RemoveLinkRequest("https://dot.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42L))
                .andExpect(jsonPath("$.url").value("https://dot.com"));

        verify(linksRepository, times(1)).removeLink(42L);
    }

    @Test
    void removeLinkWrongShema() throws Exception {
        mockMvc.perform(
                        delete("/links")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Tg-Chat-Id", 1L)
                                .content(
                                        """
                    {
                      "wrongField": "url"
                    }
                    """))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.exceptionName").value("HttpMessageNotReadableException"))
                .andExpect(jsonPath("$.exceptionMessage").exists())
                .andExpect(jsonPath("$.stacktrace").isArray());

        verifyNoInteractions(chatRepository);
        verifyNoInteractions(linksRepository);
    }

    @Test
    void removeLinkNotFound() throws Exception {
        when(chatRepository.getLinks(1L)).thenReturn(List.of(42L, 43L));
        when(linksRepository.getLink(42L))
                .thenReturn(new LinkRecord(
                        42L,
                        new URI("https://dot.com"),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        LinkType.GITHUB));
        when(linksRepository.getLink(43L))
                .thenReturn(new LinkRecord(
                        43L,
                        new URI("https://another.com"),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        LinkType.STACK_OVERFLOW));
        mockMvc.perform(delete("/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Tg-Chat-Id", 1L)
                        .content(objectMapper.writeValueAsString(new RemoveLinkRequest("https://doot.com"))))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.description").value("Ссылка не найдена"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.exceptionName").value("NotFoundException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Не существует ссылки: https://doot.com"))
                .andExpect(jsonPath("$.stacktrace").isArray());

        verify(linksRepository, never()).removeLink(anyLong());
    }
}
