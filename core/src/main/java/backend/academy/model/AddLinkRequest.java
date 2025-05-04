package backend.academy.model;

import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Builder;

@Builder(builderClassName = "Builder")
public record AddLinkRequest(
        @Pattern(regexp = "^(https?)://[\\w.-]+(?:\\.[\\w]+)+(:\\d+)?(/.*)?$", message = "Некорректный URL")
                String link,
        List<String> tags,
        List<String> filters) {}
