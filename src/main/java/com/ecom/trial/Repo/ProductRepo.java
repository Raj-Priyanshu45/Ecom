package com.ecom.trial.Repo;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.trial.Models.Products;

public interface ProductRepo extends JpaRepository<Products, Integer>{
    
    Optional<Products> findByIdAndSeller_Id(int id, int sellerId);

    
    Page<Products> findByTags_Slug(String slug, Pageable pageable);
}