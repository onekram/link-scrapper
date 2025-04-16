package backend.academy.bot.service;

import backend.academy.bot.client.ChatClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatClient chatClient;

    public void registerChat(Long id) {
        chatClient.registerChat(id);
    }

    public void unregisterChat(Long id) {
        chatClient.unRegisterChat(id);
    }
}
