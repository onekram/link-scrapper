package backend.academy.bot.controller;

import backend.academy.bot.service.UpdateService;
import backend.academy.bot.test.utils.TestUtil;
import backend.academy.model.LinkUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Description;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(UpdatesController.class)
class UpdatesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UpdateService updateService;

    @Test
    @Description("Updates for chatIds 123 and 456")
    void happyPath() throws Exception {
        LinkUpdate linkUpdate = TestUtil.generateLinkUpdate(123L, 456L);

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isOk());

        verify(updateService, only()).updateProcess(linkUpdate);
    }

    @Test
    @Description("Error for invalid parameters")
    void wrongParameters() throws Exception {
        String request =
                """
            {
                "id": "100",
                "url": "https://link.com",
                "description": "text",
                "tgChatIds": "wrong_id"
            }""";

        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.exceptionName").exists())
                .andExpect(jsonPath("$.exceptionMessage").exists())
                .andExpect(jsonPath("$.stacktrace").isArray());

        verifyNoInteractions(updateService);
    }
}
