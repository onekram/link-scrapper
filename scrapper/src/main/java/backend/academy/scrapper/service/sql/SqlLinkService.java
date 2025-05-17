package backend.academy.scrapper.service.sql;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.client.model.Created;
import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.record.LinkRecord;
import backend.academy.scrapper.repository.sql.SqlChatRepository;
import backend.academy.scrapper.repository.sql.SqlFilterRepository;
import backend.academy.scrapper.repository.sql.SqlLinkRepository;
import backend.academy.scrapper.repository.sql.SqlSubscriptionRepository;
import backend.academy.scrapper.repository.sql.SqlTagRepository;
import backend.academy.scrapper.service.LinksService;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "features.orm.enabled", havingValue = "false")
public class SqlLinkService implements LinksService {
    private final SqlFilterRepository sqlFilterRepository;
    private final SqlTagRepository sqlTagRepository;
    private final SqlChatRepository sqlChatRepository;
    private final SqlLinkRepository sqlLinkRepository;
    private final SqlSubscriptionRepository sqlSubscriptionRepository;

    @Value("${pagination.page-size}")
    private int PAGE_SIZE;

    @Transactional
    @Override
    public ListLinksResponse listAll(Long tgChatId) {
        List<LinkResponse> linkResponses = sqlLinkRepository.findAllByChatId(tgChatId).stream()
                .map(result -> {
                    List<String> tags = sqlTagRepository.findAllBySubscriptionId(result.subscriptionId());
                    List<String> filters = sqlFilterRepository.findAllBySubscriptionId(result.subscriptionId());
                    return new LinkResponse(result.linkId(), result.url(), tags, filters);
                })
                .toList();
        return new ListLinksResponse(linkResponses, linkResponses.size());
    }

    @Transactional
    @Override
    public LinkResponse addLink(Long tgChatId, AddLinkRequest request) {
        sqlChatRepository.saveIfAbsentById(tgChatId);
        Long linkId = sqlLinkRepository.saveIfAbsentByUrl(request.link());

        Set<Long> tagIds = request.tags().stream()
                .map(sqlTagRepository::saveIfAbsentByName)
                .collect(Collectors.toSet());

        Set<Long> filterIds = request.filters().stream()
                .map(sqlFilterRepository::saveIfAbsentByName)
                .collect(Collectors.toSet());

        long subId = sqlSubscriptionRepository.saveIfAbsentByChatIdAndLinkId(tgChatId, linkId);

        sqlSubscriptionRepository.deleteAssociationTags(subId);
        sqlSubscriptionRepository.associateTags(subId, tagIds);

        sqlSubscriptionRepository.deleteAssociationFilters(subId);
        sqlSubscriptionRepository.associateFilters(subId, filterIds);

        return new LinkResponse(linkId, request.link(), List.copyOf(request.tags()), List.copyOf(request.filters()));
    }

    @Transactional
    @Override
    public LinkResponse removeLink(Long tgChatId, RemoveLinkRequest request) {
        Long linkId;
        try {
            linkId = sqlLinkRepository.findByUrl(request.link());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Не существует ссылки: %s".formatted(request.link()));
        }

        Long subId;
        try {
            subId = sqlSubscriptionRepository.findByChatIdAndLinkId(tgChatId, linkId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Не существует ссылки: %s".formatted(linkId));
        }

        List<String> tags = sqlTagRepository.findAllBySubscriptionId(subId);
        List<String> filters = sqlFilterRepository.findAllBySubscriptionId(subId);

        sqlSubscriptionRepository.deleteById(subId);
        sqlLinkRepository.deleteByIdIfNoAssociatedSubscriptions(linkId);

        return new LinkResponse(linkId, request.link(), tags, filters);
    }

    @Override
    public Stream<LinkRecord> findAllByType(LinkType linkType) {
        return Stream.iterate(0, n -> n + 1)
                .map(n -> sqlLinkRepository.findAllByType(linkType, n * PAGE_SIZE, PAGE_SIZE))
                .takeWhile(pageList -> !pageList.isEmpty())
                .flatMap(List::stream);
    }

    @Transactional
    @Override
    public void update(LinkRecord linkRecord, Stream<? extends Created> createdStream) {
        Instant max = createdStream
                .map(Created::createdAt)
                .max(Comparator.naturalOrder())
                .orElse(linkRecord.updatedAt());
        sqlLinkRepository.updateUpdatedAtByUrl(linkRecord.url(), max);
    }
}
