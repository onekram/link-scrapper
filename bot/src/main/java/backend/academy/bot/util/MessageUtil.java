package backend.academy.bot.util;

import backend.academy.model.LinkResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageUtil {
    public static String linkMessage(LinkResponse linkResponse) {
        return String.format(
            "\uD83C\uDF10 %s\n-- \uD83D\uDD16 %s\n-- \uD83D\uDD0D %s",
            linkResponse.getUrl(),
            String.join(" ", linkResponse.getTags()),
            String.join(" ", linkResponse.getFilters()));
    }
}
