package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.OutboxEvent;
import backend.academy.linktracker.scrapper.dto.OutboxEventUpdateDto;
import java.util.List;

public interface OutboxEventRepository {

    void addAll(List<OutboxEvent> outboxEvents);

    List<OutboxEvent> findBatchPendingMessagesAndSetProcessing(int batchSize, String topic);

    void updateMessageStatusBatch(List<OutboxEventUpdateDto> outboxEventUpdateDtos);

    void markStuckPendingAsFail(int minutes);
}
