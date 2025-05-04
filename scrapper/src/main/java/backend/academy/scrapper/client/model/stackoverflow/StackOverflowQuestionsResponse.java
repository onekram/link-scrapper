package backend.academy.scrapper.client.model.stackoverflow;

import java.util.List;

public record StackOverflowQuestionsResponse(List<Question> items) {}
