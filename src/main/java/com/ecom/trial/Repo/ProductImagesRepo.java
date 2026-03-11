package com.ecom.trial.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.trial.Models.ProductImages;
import java.util.List;


public interface ProductImagesRepo extends JpaRepository<ProductImages, Integer>{
 
    List<ProductImages> findByProductId(int id);

    boolean existsByProductIdAndId(int productId , int id);

    void deleteByProductId(int id);
}
