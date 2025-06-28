// package should match your folder structure
package com.example.common.dtos;

// This record represents the initial order request from a user.
// It's a simple, immutable data carrier.
public record OrderRequest(String orderId, String userId, String productId, int quantity, double amount) {}