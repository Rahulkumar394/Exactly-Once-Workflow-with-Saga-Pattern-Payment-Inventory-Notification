package com.example.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.common.dtos.EventStatus;
import com.example.common.dtos.SagaEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

@Service
public class PaymentProcessor {
    private final KafkaTemplate<String, SagaEvent> kafkaTemplate;

    public PaymentProcessor(KafkaTemplate<String, SagaEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "saga.step.payment", groupId = "payment-group")
    @Transactional("kafkaTransactionManager")
    public void handlePayment(SagaEvent event) {
        System.out.println("Saga " + event.sagaId() + ": Processing payment...");
        boolean paymentSuccess = true; // Simulate success
        
        SagaEvent replyEvent = new SagaEvent(event.sagaId(), event.orderId(), event.payload(), 
            paymentSuccess ? EventStatus.SUCCESS : EventStatus.FAILURE);
            
        kafkaTemplate.send("saga.reply.payment", replyEvent);
    }

    @KafkaListener(topics = "saga.compensate.payment", groupId = "payment-group")
    @Transactional("kafkaTransactionManager")
    public void handleCompensation(SagaEvent event) {
        System.err.println("Saga " + event.sagaId() + ": Compensating payment (refunding)...");
        // Business logic to refund payment
        
        SagaEvent replyEvent = new SagaEvent(event.sagaId(), event.orderId(), event.payload(), EventStatus.COMPENSATED);
        kafkaTemplate.send("saga.reply.payment", replyEvent);
    }
}