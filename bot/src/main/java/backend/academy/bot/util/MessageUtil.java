package backend.academy.bot.util;

import backend.academy.model.LinkResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageUtil {
    public static String linkMessage(LinkResponse linkResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("\uD83C\uDF10 %s".formatted(linkResponse.url()));
        if (!linkResponse.tags().isEmpty()) {
            sb.append('\n');
            sb.append("• \uD83D\uDD16 %s".formatted(linkResponse.tags()));
        }
        if (!linkResponse.filters().isEmpty()) {
            sb.append('\n');
            sb.append("• \uD83D\uDD0D %s".formatted(linkResponse.filters()));
        }
        return sb.toString();
    }
}
