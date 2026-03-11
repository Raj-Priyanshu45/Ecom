package com.ecom.trial.DTOs.ResponseDTOs;

import java.time.LocalDateTime;
import java.util.List;

import com.ecom.trial.Models.Tags;


public record SingleProductResponse(
    String name , 
    String description , 
    String sellerId ,
    LocalDateTime addedAt , 
    LocalDateTime modifiedAt ,
    List<Tags> tags
) {
}