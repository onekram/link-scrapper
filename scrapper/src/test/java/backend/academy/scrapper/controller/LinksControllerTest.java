package backend.academy.scrapper.controller;

import static backend.academy.scrapper.test.util.TestUtil.generateLinkResponse;
import static backend.academy.scrapper.test.util.TestUtil.generateListLinksResponse;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.service.LinksService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(LinksController.class)
class LinksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LinksService linksService;

    @Test
    void getLinksOk() throws Exception {
        when(linksService.listAll(1L))
                .thenReturn(generateListLinksResponse(
                        Map.entry(42L, "https://dot.com"), Map.entry(43L, "https://another.com")));

        mockMvc.perform(get("/links").contentType(MediaType.APPLICATION_JSON).header("Tg-Chat-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(2));

        verify(linksService, times(1)).listAll(anyLong());
    }

    @Test
    void getLinksNegaviveTgChatId() throws Exception {
        mockMvc.perform(get("/links").contentType(MediaType.APPLICATION_JSON).header("Tg-Chat-Id", -1L))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.exceptionName").value("ConstraintViolationException"))
                .andExpect(jsonPath("$.exceptionMessage", containsString("Невалидный идентификатор чата")))
                .andExpect(jsonPath("$.stacktrace").isArray());

        verifyNoInteractions(linksService);
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

        verifyNoInteractions(linksService);
    }

    @Test
    void addLinkOk() throws Exception {
        AddLinkRequest request =
                new AddLinkRequest("https://third.com", Collections.emptyList(), Collections.emptyList());
        when(linksService.addLink(1L, request)).thenReturn(generateLinkResponse(42L, "https://third.com"));

        mockMvc.perform(post("/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Tg-Chat-Id", 1L)
                        .content(objectMapper.writeValueAsString(new AddLinkRequest(
                                "https://third.com", Collections.emptyList(), Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").value("https://third.com"));

        verify(linksService, times(1)).addLink(anyLong(), eq(request));
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

        verifyNoInteractions(linksService);
    }

    @Test
    void removeLinkOk() throws Exception {
        RemoveLinkRequest request = new RemoveLinkRequest("https://dot.com");
        when(linksService.removeLink(1L, request)).thenReturn(generateLinkResponse(42L, "https://dot.com"));

        mockMvc.perform(delete("/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Tg-Chat-Id", 1L)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42L))
                .andExpect(jsonPath("$.url").value("https://dot.com"));

        verify(linksService, times(1)).removeLink(1L, request);
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

        verifyNoInteractions(linksService);
    }

    @Test
    void removeLinkNotFound() throws Exception {
        RemoveLinkRequest request = new RemoveLinkRequest("https://doot.com");
        when(linksService.removeLink(1L, request))
                .thenThrow(new NotFoundException("Не существует ссылки: %s".formatted(request.link())));

        mockMvc.perform(delete("/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Tg-Chat-Id", 1L)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.description").value("Запрашиваемый ресурс не найден"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.exceptionName").value("NotFoundException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Не существует ссылки: https://doot.com"))
                .andExpect(jsonPath("$.stacktrace").isArray());
    }
}
