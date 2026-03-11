package com.ecom.trial.DTOs.ResponseDTOs;

import java.math.BigDecimal;
import java.util.List;

import com.ecom.trial.Models.Tags;

public record AllProductResponse(
    int id , 
    String name , 
    String shortDesc,
    String imageUrl , 
    BigDecimal price ,
    boolean inStock ,
    List<Tags> tags 
    //,double rating
) {
    
}
