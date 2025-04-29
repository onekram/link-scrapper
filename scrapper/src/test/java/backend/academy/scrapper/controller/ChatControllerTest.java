package backend.academy.scrapper.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.record.ChatRecord;
import backend.academy.scrapper.service.ChatService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(ChatController.class)
@Import({ChatService.class})
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatRepository chatRepository;

    @Test
    void registerChatOk() throws Exception {
        mockMvc.perform(post("/tg-chat/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(chatRepository, times(1)).saveUser(1L);
    }

    @Test
    void registerChatErrorTypeMismatch() throws Exception {
        mockMvc.perform(post("/tg-chat/abc").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentTypeMismatchException"))
                .andExpect(jsonPath("$.exceptionMessage").exists())
                .andExpect(jsonPath("$.stacktrace").isArray());

        verifyNoInteractions(chatRepository);
    }

    @Test
    void registerChatErrorNegativeId() throws Exception {
        mockMvc.perform(post("/tg-chat/-1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.exceptionName").value("BadRequestException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Невалидный идентификатор чата: -1"))
                .andExpect(jsonPath("$.stacktrace").isArray());

        verifyNoInteractions(chatRepository);
    }

    @Test
    void UnRegisterChatOk() throws Exception {
        when(chatRepository.removeUser(1L)).thenReturn(new ChatRecord(1L, List.of(42L)));

        mockMvc.perform(delete("/tg-chat/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(chatRepository, times(1)).removeUser(1L);
    }

    @Test
    void UnRegisterChatNoSuchChat() throws Exception {
        when(chatRepository.removeUser(1L)).thenReturn(null);

        mockMvc.perform(delete("/tg-chat/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.description").value("Запрашиваемый ресурс не найден"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.exceptionName").value("NotFoundException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Не существует чата с ID: 1"))
                .andExpect(jsonPath("$.stacktrace").isArray());
    }

    @Test
    void unregisterChatErrorTypeMismatch() throws Exception {
        mockMvc.perform(delete("/tg-chat/abs").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentTypeMismatchException"))
                .andExpect(jsonPath("$.exceptionMessage").exists())
                .andExpect(jsonPath("$.stacktrace").isArray());

        verifyNoInteractions(chatRepository);
    }

    @Test
    void unregisterChatErrorNegativeId() throws Exception {
        mockMvc.perform(delete("/tg-chat/-1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.exceptionName").value("BadRequestException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Невалидный идентификатор чата: -1"))
                .andExpect(jsonPath("$.stacktrace").isArray());

        verifyNoInteractions(chatRepository);
    }
}
