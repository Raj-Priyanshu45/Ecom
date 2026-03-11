package com.ecom.trial.DTOs.ResponseDTOs;

import java.util.List;

public record  ChangeImageGETResponse(
     List<?> imageList,
     int imageCount , 
    String primary 
) {
    
}
