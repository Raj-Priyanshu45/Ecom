package com.ecom.trial.DTOs.RequestDTOs;

public record RegisterRequest(
        String username,
        String email,
        String password
) {}