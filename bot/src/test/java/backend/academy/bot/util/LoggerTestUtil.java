package backend.academy.bot.util;

import static org.slf4j.LoggerFactory.getLogger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LoggerTestUtil {
    public static ListAppender<ILoggingEvent> getListAppender(Class<?> clazz) {
        Logger logger = (Logger) getLogger(clazz);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        return listAppender;
    }

    public static boolean appenderContainsLog(ListAppender<ILoggingEvent> appender, Level level, String message) {
        return appender.list.stream()
                .anyMatch(event -> event.getLevel().equals(level)
                        && event.getFormattedMessage().equals(message));
    }
}
