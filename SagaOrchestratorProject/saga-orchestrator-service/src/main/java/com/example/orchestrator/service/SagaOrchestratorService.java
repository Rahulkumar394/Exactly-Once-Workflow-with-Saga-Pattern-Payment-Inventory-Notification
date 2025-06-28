package com.example.orchestrator.service;

import com.example.common.dtos.OrderRequest;
import com.example.common.dtos.SagaEvent;
import com.example.orchestrator.model.SagaState;
import com.example.orchestrator.model.SagaStatus;
import com.example.orchestrator.repository.SagaStateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SagaOrchestratorService {
    private final SagaStateRepository sagaStateRepository;
    private final KafkaTemplate<String, SagaEvent> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SagaOrchestratorService(SagaStateRepository repository, KafkaTemplate<String, SagaEvent> template) {
        this.sagaStateRepository = repository;
        this.kafkaTemplate = template;
    }

    // This method starts the whole process.
    // @Transactional ensures that saving to the DB and sending to Kafka is one atomic operation.
    @Transactional("transactionManager") // Use the default ChainedKafkaTransactionManager
    public void startSaga(String sagaId, OrderRequest orderRequest) {
        try {
            // 1. Create and save the initial state of the Saga.
            String orderPayload = objectMapper.writeValueAsString(orderRequest);
            SagaState sagaState = new SagaState();
            sagaState.setSagaId(sagaId);
            sagaState.setOrderId(orderRequest.orderId());
            sagaState.setCurrentStatus(SagaStatus.PAYMENT_PROCESSING);
            sagaState.setPayload(orderPayload);
            sagaStateRepository.save(sagaState);

            // 2. Create the first event for the Payment Service.
            SagaEvent paymentEvent = new SagaEvent(sagaId, orderRequest.orderId(), orderPayload, null);

            // 3. Send the event. It will only be sent if the DB save succeeds.
            kafkaTemplate.send("saga.step.payment", paymentEvent);
            System.out.println("Saga " + sagaId + " started. Sent payment request.");

        } catch (Exception e) {
            System.err.println("Error starting saga: " + e.getMessage());
            // The transaction will automatically roll back.
            throw new RuntimeException("Failed to start saga", e);
        }
    }
}