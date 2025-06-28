package com.example.inventory.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.common.dtos.EventStatus;
import com.example.common.dtos.OrderRequest;
import com.example.common.dtos.SagaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class InventoryProcessor {
	private final KafkaTemplate<String, SagaEvent> kafkaTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public InventoryProcessor(KafkaTemplate<String, SagaEvent> template) {
		this.kafkaTemplate = template;
	}

	@KafkaListener(topics = "saga.step.inventory", groupId = "inventory-group")
	@Transactional("kafkaTransactionManager")
	public void handleInventoryUpdate(SagaEvent event) {
		System.out.println("Saga " + event.sagaId() + ": Updating inventory...");
		SagaEvent replyEvent;
		try {
			OrderRequest order = objectMapper.readValue(event.payload(), OrderRequest.class);

			// SIMULATE FAILURE: if productId is "FAIL-ME", the inventory update fails.
			if ("FAIL-ME".equalsIgnoreCase(order.productId())) {
				System.err.println("Saga " + event.sagaId() + ": Inventory update failed (SIMULATED).");
				replyEvent = new SagaEvent(event.sagaId(), event.orderId(), event.payload(), EventStatus.FAILURE);
			} else {
				System.out.println("Saga " + event.sagaId() + ": Inventory updated successfully.");
				replyEvent = new SagaEvent(event.sagaId(), event.orderId(), event.payload(), EventStatus.SUCCESS);
			}
		} catch (Exception e) {
			replyEvent = new SagaEvent(event.sagaId(), event.orderId(), event.payload(), EventStatus.FAILURE);
		}
		kafkaTemplate.send("saga.reply.inventory", replyEvent);
	}

	@KafkaListener(topics = "saga.compensate.inventory", groupId = "inventory-group")
	@Transactional("kafkaTransactionManager")
	public void handleCompensation(SagaEvent event) {
		System.err.println("Saga " + event.sagaId() + ": Compensating inventory (restoring stock)...");
		// No reply needed for compensation in this step, but you can send one if
		// required.
	}
}