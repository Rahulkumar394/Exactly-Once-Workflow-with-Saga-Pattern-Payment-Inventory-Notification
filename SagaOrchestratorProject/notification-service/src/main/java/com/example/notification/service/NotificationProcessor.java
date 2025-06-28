package com.example.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.common.dtos.EventStatus;
import com.example.common.dtos.SagaEvent;

@Service
public class NotificationProcessor {
	private final KafkaTemplate<String, SagaEvent> kafkaTemplate;

	public NotificationProcessor(KafkaTemplate<String, SagaEvent> template) {
		this.kafkaTemplate = template;
	}

	@KafkaListener(topics = "saga.step.notification", groupId = "notification-group")
	@Transactional("kafkaTransactionManager")
	public void handleNotification(SagaEvent event) {
		System.out.println("Saga " + event.sagaId() + ": Sending notification...");
		boolean notificationSuccess = true; // Simulate success

		SagaEvent replyEvent = new SagaEvent(event.sagaId(), event.orderId(), event.payload(),
				notificationSuccess ? EventStatus.SUCCESS : EventStatus.FAILURE);

		kafkaTemplate.send("saga.reply.notification", replyEvent);
	}
}