package com.banktransfer.feedback_service.kafka;

import com.banktransfer.feedback_service.event.ComplaintCreatedEvent;
import com.banktransfer.feedback_service.event.ComplaintResolvedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ComplaintEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCreated(ComplaintCreatedEvent event) {
        log.info("Publication ComplaintCreated : id={}, priority={}", event.getComplaintId(), event.getPriority());
        kafkaTemplate.send("complaint-created-topic", event.getComplaintId().toString(), event);
    }

    public void publishResolved(ComplaintResolvedEvent event) {
        log.info("Publication ComplaintResolved : id={}", event.getComplaintId());
        kafkaTemplate.send("complaint-resolved-topic", event.getComplaintId().toString(), event);
    }
}