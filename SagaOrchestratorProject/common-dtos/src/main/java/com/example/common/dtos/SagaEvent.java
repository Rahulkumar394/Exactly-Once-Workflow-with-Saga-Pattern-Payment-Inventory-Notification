// package should match your folder structure
package com.example.common.dtos;

// This is the event that will be passed between services via Kafka.
// It contains the sagaId to track the entire transaction flow.
public record SagaEvent(String sagaId, String orderId, String payload, EventStatus status) {}