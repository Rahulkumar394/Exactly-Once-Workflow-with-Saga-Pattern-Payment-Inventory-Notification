package com.example.orchestrator.repository;

import com.example.orchestrator.model.SagaState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SagaStateRepository extends JpaRepository<SagaState, String> {
    Optional<SagaState> findBySagaId(String sagaId);
}