package com.ecom.trial.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecom.trial.Models.ProductImages;
import java.util.List;

@Repository
public interface ProductImagesRepo extends JpaRepository<ProductImages, Integer>{
 
    List<ProductImages> findByProductId(int id);

    boolean existsByProductIdAndId(int productId , int id);

    void deleteByProductId(int id);

    boolean existsByProductIdAndPrimaryTrue(int productId);

    @Modifying
    @Query("UPDATE ProductImages p SET p.primary = false WHERE p.id = :productId")
    void clearPrimary(@Param("productId") int id);

    @Modifying
    @Query("UPDATE ProductImages p SET p.primary = true WHERE p.id = :productId")
    void updatePrimary(@Param("productId") int id);
}
