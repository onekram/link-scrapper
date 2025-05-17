package backend.academy.scrapper.client.stackoverflow;

import backend.academy.scrapper.client.model.stackoverflow.StackOverflowQuestionsResponse;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface StackOverflowQuestionClient {

    @GetExchange("/questions/{ids}?site=stackoverflow")
    StackOverflowQuestionsResponse getQuestionsByIds(@PathVariable String ids);

    default StackOverflowQuestionsResponse getQuestionsByIds(List<String> ids) {
        return getQuestionsByIds(String.join(";", ids));
    }
}
