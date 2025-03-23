package backend.academy.bot.controller;

import backend.academy.bot.util.TestUtil;
import backend.academy.model.LinkUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UpdatesController.class)
class UpdatesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TelegramBot telegramBot;

    @Test
    @Description("Updates for chatIds 123 and 456")
    void happyPath() throws Exception {
        LinkUpdate request = TestUtil.generateLinkUpdate(123L, 456L);

        mockMvc.perform(post("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot, times(2)).execute(messageCaptor.capture(), any());

        List<SendMessage> sentMessages = messageCaptor.getAllValues();


        assertThat(sentMessages.getFirst()).extracting(TestUtil::getId).isEqualTo(123L);
        assertThat(sentMessages.getLast()).extracting(TestUtil::getId).isEqualTo(456L);

        assertThat(sentMessages.getFirst()).extracting(TestUtil::getText).isEqualTo("Updates detected");
        assertThat(sentMessages.getLast()).extracting(TestUtil::getText).isEqualTo("Updates detected");
    }

    @Test
    @Description("Error for invalid parameters")
    void wrongParameters() throws Exception {
        String request = """
            {
                "id": "100",
                "url": "https://link.com",
                "description": "text",
                "tgChatIds": "wrong_id"
            }""";

        mockMvc.perform(post("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.exceptionName").exists())
            .andExpect(jsonPath("$.exceptionMessage").exists())
            .andExpect(jsonPath("$.stacktrace").isArray());

        verify(telegramBot, never()).execute(any(SendMessage.class), any());
    }
}
