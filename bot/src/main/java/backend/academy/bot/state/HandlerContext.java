package backend.academy.bot.state;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;

public record HandlerContext(Message message, TelegramBot bot, State state) {}
