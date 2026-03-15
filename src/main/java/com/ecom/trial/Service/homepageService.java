package com.ecom.trial.Service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.ecom.trial.Models.Tags;
import com.ecom.trial.Repo.ProductRepo;
import com.ecom.trial.Repo.TagRepo;
import com.ecom.trial.Repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class homepageService {
    
    private final ProductRepo productRepo;
    private final TagRepo tagRepo;
    private final UserRepo userRepo;
    
    @Cacheable(value = "tags")
    private List<Tags> getAllTags(){
        return tagRepo.findAll();
    }

    
}
