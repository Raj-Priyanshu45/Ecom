package com.ecom.trial.DTOs.ResponseDTOs;

import java.util.List;

public record ApiResponse<T>(
    List<T> products
    , int size 
    , int page 
    , long totalElements
    , int totalPages
    , boolean last
) {
    
}