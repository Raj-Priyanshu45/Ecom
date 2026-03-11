package com.ecom.trial.Controller;

import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.trial.DTOs.RequestDTOs.CreateProducts;
import com.ecom.trial.DTOs.RequestDTOs.ModifyProducts;
import com.ecom.trial.DTOs.ResponseDTOs.Response;
import com.ecom.trial.Service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    //create or add products can be done only by seller
    //and admin has all the access
    
    @PostMapping("/add")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<?> addProducts(@RequestBody @Valid CreateProducts products , Authentication authentication){

        return ResponseEntity.status(201).body(productService.saveProduct(products, authentication));
    }

    @PutMapping("/modify/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<?> modifyProducts(@PathVariable int id , 
        @RequestBody @Valid ModifyProducts products , Authentication authentication){

        return ResponseEntity.status(200).body(productService.modifyProduct(id , products, authentication));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPPORT')")
    public ResponseEntity<?> deleteProduct(@PathVariable int id , Authentication authentication){
        String name = productService.deleteProduct(id , authentication);

        return ResponseEntity.status(200).body(new Response("product "+name+" successfully deleted"));
    }

    @PostMapping("/{productId}/upload")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadPicture(
        @RequestParam("image") MultipartFile[] files ,
        @PathVariable int productId ,
        @RequestParam int primaryKey,
        Authentication authentication) throws IOException{

            if(primaryKey < 0 || primaryKey >= files.length){
                return ResponseEntity.badRequest().body("Invalid Primary Image");
            }

            productService.uploadImage(productId , primaryKey , files , authentication);

            return ResponseEntity.ok("File uploaded Successfully");
    }
}