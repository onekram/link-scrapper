package backend.academy.model;

import java.util.List;
import lombok.Builder;

@Builder(builderClassName = "Builder")
public record AddLinkRequest(String link, List<String> tags, List<String> filters) {}
