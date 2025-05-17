package backend.academy.scrapper.service.sql;

import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.repository.sql.SqlChatRepository;
import backend.academy.scrapper.repository.sql.SqlLinkRepository;
import backend.academy.scrapper.service.ChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "features.orm.enabled", havingValue = "false")
public class SqlChatService implements ChatService {

    private final SqlChatRepository sqlChatRepository;
    private final SqlLinkRepository sqlLinkRepository;

    @Override
    public void register(Long tgChatId) {
        sqlChatRepository.saveIfAbsentById(tgChatId);
    }

    @Override
    @Transactional
    public void unRegister(Long tgChatId) {
        if (!sqlChatRepository.existsById(tgChatId)) {
            throw new NotFoundException(String.format("Не существует чата с ID: %s", tgChatId));
        }
        List<Long> linkIds = sqlLinkRepository.findAllIdsByChatId(tgChatId);
        sqlChatRepository.deleteById(tgChatId);
        linkIds.forEach(sqlLinkRepository::deleteByIdIfNoAssociatedSubscriptions);
    }
}
