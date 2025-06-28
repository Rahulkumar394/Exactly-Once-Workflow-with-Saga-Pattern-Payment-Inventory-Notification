package com.example.orchestrator.controller;

import com.example.common.dtos.OrderRequest;
import com.example.orchestrator.service.SagaOrchestratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final SagaOrchestratorService orchestratorService;

    public OrderController(SagaOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    // This endpoint starts the whole Saga process
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
        String sagaId = UUID.randomUUID().toString();
        orchestratorService.startSaga(sagaId, orderRequest);
        return ResponseEntity.ok("Saga process started with ID: " + sagaId);
    }
}