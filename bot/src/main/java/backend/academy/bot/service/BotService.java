package backend.academy.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class BotService {
    private final TelegramBot telegramBot;

    public BotService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }


    @PostConstruct
    public void initBot() {


        telegramBot.setUpdatesListener(updates -> {
            try {
                for (Update update : updates) {
                    if (update.message() != null) {
                        long chatId = update.message().chat().id();
                        telegramBot.execute(new SendMessage(chatId, "Привет"));
                    }
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            } catch (Exception e) {
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        }, Throwable::printStackTrace);
    }

}
