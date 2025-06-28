package com.example.orchestrator.service;

import com.example.common.dtos.EventStatus;
import com.example.common.dtos.SagaEvent;
import com.example.orchestrator.model.SagaState;
import com.example.orchestrator.model.SagaStatus;
import com.example.orchestrator.repository.SagaStateRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SagaReplyListener {
    private final SagaStateRepository repository;
    private final KafkaTemplate<String, SagaEvent> kafkaTemplate;

    public SagaReplyListener(SagaStateRepository repository, KafkaTemplate<String, SagaEvent> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "saga.reply.payment", groupId = "orchestrator-group")
    @Transactional("transactionManager")
    public void handlePaymentReply(SagaEvent event) {
        repository.findBySagaId(event.sagaId()).ifPresent(sagaState -> {
            if (event.status() == EventStatus.SUCCESS) {
                System.out.println("Saga " + event.sagaId() + ": Payment successful. Requesting inventory update.");
                sagaState.setCurrentStatus(SagaStatus.INVENTORY_PROCESSING);
                kafkaTemplate.send("saga.step.inventory", event);
            } else { // FAILURE or COMPENSATED
                 System.err.println("Saga " + event.sagaId() + ": Payment failed or compensated. Saga rolled back.");
                sagaState.setCurrentStatus(SagaStatus.ROLLED_BACK);
            }
            repository.save(sagaState);
        });
    }

    @KafkaListener(topics = "saga.reply.inventory", groupId = "orchestrator-group")
    @Transactional("transactionManager")
    public void handleInventoryReply(SagaEvent event) {
        repository.findBySagaId(event.sagaId()).ifPresent(sagaState -> {
            if (event.status() == EventStatus.SUCCESS) {
                System.out.println("Saga " + event.sagaId() + ": Inventory updated. Requesting notification.");
                sagaState.setCurrentStatus(SagaStatus.NOTIFICATION_PROCESSING);
                kafkaTemplate.send("saga.step.notification", event);
            } else { // FAILURE
                System.err.println("Saga " + event.sagaId() + ": Inventory failed. Compensating payment.");
                sagaState.setCurrentStatus(SagaStatus.COMPENSATING);
                kafkaTemplate.send("saga.compensate.payment", event);
            }
            repository.save(sagaState);
        });
    }

    @KafkaListener(topics = "saga.reply.notification", groupId = "orchestrator-group")
    @Transactional("transactionManager")
    public void handleNotificationReply(SagaEvent event) {
        repository.findBySagaId(event.sagaId()).ifPresent(sagaState -> {
            if (event.status() == EventStatus.SUCCESS) {
                System.out.println("Saga " + event.sagaId() + ": Notification sent. SAGA COMPLETED SUCCESSFULLY.");
                sagaState.setCurrentStatus(SagaStatus.COMPLETED);
            } else { // FAILURE
                System.err.println("Saga " + event.sagaId() + ": Notification failed. Compensating inventory.");
                sagaState.setCurrentStatus(SagaStatus.COMPENSATING);
                // In this example, we roll back everything if notification fails.
                kafkaTemplate.send("saga.compensate.inventory", event);
            }
            repository.save(sagaState);
        });
    }
}