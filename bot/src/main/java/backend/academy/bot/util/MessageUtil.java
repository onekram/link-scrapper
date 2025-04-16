package backend.academy.bot.util;

import backend.academy.model.LinkResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageUtil {
    public static String linkMessage(LinkResponse linkResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("\uD83C\uDF10 %s".formatted(linkResponse.getUrl()));
        if (!linkResponse.getTags().isEmpty()) {
            sb.append('\n');
            sb.append("• \uD83D\uDD16 %s".formatted(linkResponse.getTags()));
        }
        if (!linkResponse.getFilters().isEmpty()) {
            sb.append('\n');
            sb.append("• \uD83D\uDD0D %s".formatted(linkResponse.getFilters()));
        }
        return sb.toString();
    }
}
