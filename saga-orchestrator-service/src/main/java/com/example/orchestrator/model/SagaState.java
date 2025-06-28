package com.example.orchestrator.model;

import jakarta.persistence.*;

// This is a database entity to store the state of each saga
@Entity
public class SagaState {
    @Id
    private String sagaId; // Use the saga ID as the primary key
    private String orderId;
    @Enumerated(EnumType.STRING)
    private SagaStatus currentStatus;
    @Column(length = 1024) // Store the original order request as JSON
    private String payload;

    // Getters and Setters...
    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public SagaStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(SagaStatus currentStatus) { this.currentStatus = currentStatus; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}