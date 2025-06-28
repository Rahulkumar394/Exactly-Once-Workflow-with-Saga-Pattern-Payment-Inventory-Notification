// package should match your folder structure
package com.example.common.dtos;

// Represents the status of an event sent back from a participant service.
public enum EventStatus {
    SUCCESS,      // The step was successful
    FAILURE,      // The step failed
    COMPENSATED   // The compensation for a step was successful
}