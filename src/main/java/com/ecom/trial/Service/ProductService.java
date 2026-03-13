package com.ecom.trial.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.trial.DTOs.RequestDTOs.CreateProducts;
import com.ecom.trial.DTOs.RequestDTOs.ModifyProducts;
import com.ecom.trial.DTOs.ResponseDTOs.AddProduct;
import com.ecom.trial.DTOs.ResponseDTOs.ChangeImageGETResponse;
import com.ecom.trial.ExceptionalHandling.AccessDeniedException;
import com.ecom.trial.ExceptionalHandling.ImageNotFoundException;
import com.ecom.trial.ExceptionalHandling.ProductNotFoundException;
import com.ecom.trial.ExceptionalHandling.UserNotFoundException;
import com.ecom.trial.Models.ProductImages;
import com.ecom.trial.Models.Products;
import com.ecom.trial.Models.Tags;
import com.ecom.trial.Models.User;
import com.ecom.trial.Repo.ProductImagesRepo;
import com.ecom.trial.Repo.ProductRepo;
import com.ecom.trial.Repo.TagRepo;
import com.ecom.trial.Repo.UserRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepo productRepo;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserRepo userRepo;
    private final TagRepo tagRepo;
    private final ProductImagesRepo imageRepo;

    // public productService(productRepo productRepo , userRepo userRepo){
    //     this.productRepo = productRepo;
    //     this.userRepo = userRepo;
    // }

    private User getCurrentUser(Authentication authentication){
        return userRepo.findByKeyCloakId(authentication.getName()).orElseThrow(
            () -> new UserNotFoundException("User Not Found")
        );
    }

    private Products getProducts(int id){
        return productRepo.findById(id).orElseThrow(
            () -> new ProductNotFoundException("Product not found")
        );
    }

    private ProductImages getProductImages(int id){
        return imageRepo.findById(id).orElseThrow(
            ()-> new ImageNotFoundException("Image Not Found")
        );
    }

    private String toSlug(String tag) {
    return tag.trim()
              .toLowerCase()
              .replaceAll("\\s+", "-");
    }

    private List<ProductImages> getPicOfProduct(int productId){

        return imageRepo.findByProductId(productId);
    }

    public AddProduct saveProduct(CreateProducts product , Authentication authentication){

        User user = getCurrentUser(authentication);

        Set<Tags> tag = new HashSet<>();

        for (String st : product.getTags()) {

            String slug = toSlug(st);

            Tags tg = tagRepo.findBySlug(slug)
                    .orElseGet(() ->
                        tagRepo.save(
                            Tags.builder()
                                .name(st.trim().toLowerCase())
                                .slug(slug)
                                .build()
                        )
                    );

            tag.add(tg);
        }

        Products newProduct = productRepo.save(
            Products
            .builder()
            .name(product.getName())
            .price(product.getPrice())
            .description(product.getDescription())
            .seller(user)
            .count(product.getCount())
            .isDel(false)
            .tags(tag)
            .build()
        );

        return AddProduct
            .builder()
            .description(product.getDescription())
            .name(product.getName())
            .price(product.getPrice())
            .sellerId(user.getKeyCloakId())
            .productId(newProduct.getId())
            .tags(product.getTags())
            .build();
    }

    public AddProduct modifyProduct(int id , ModifyProducts product , Authentication authentication){

        User user = getCurrentUser(authentication);

        Products old = getProducts(id);

        boolean isAdmin = authCheck(authentication, "admin");

        if(!isAdmin && !old.getSeller().getKeyCloakId().equals(user.getKeyCloakId())){
            throw new AccessDeniedException("You can't modify this product");
        }
        
        Set<Tags> tag = new HashSet<>();

        List<String> tagList = new ArrayList<>();

        for (String st : product.getTags()) {

            String slug = toSlug(st);

            Tags tg = tagRepo.findBySlug(slug)
                    .orElseGet(() ->
                        tagRepo.save(
                            Tags.builder()
                                .name(st.trim().toLowerCase())
                                .slug(slug)
                                .build()
                        )
                    );

            tag.add(tg);
            tagList.add(tg.getName());
        }
        old.setDescription(product.getDescription());
        old.setName((product.getName()));
        old.setPrice(product.getPrice());
        old.setTags(tag);
        old.setCount(product.getCount());

        productRepo.save(old);

        logger.info("product modified successfully");
        return AddProduct.builder()
                .name(old.getName())
                .description(old.getDescription())
                .sellerId(authentication.getName())
                .productId(old.getId())
                .price(old.getPrice())
                .tags(tagList)
                .build();
    }
    
    public String deleteProduct(int id , Authentication authentication){

        User user = getCurrentUser(authentication);

        Products old = getProducts(id);

        boolean isAdmin = authCheck(authentication , "admin");

        boolean isSupport = authCheck(authentication , "support");


        if(!isAdmin && !isSupport && !old.getSeller().getKeyCloakId().equals(user.getKeyCloakId())){
            logger.warn("Unauthorized attempt");
            throw new AccessDeniedException("You can't delete this product");
        }

        logger.info("product successfully deleted");


        productRepo.deleteById(id);
        return old.getName();
    }

    public boolean authCheck(Authentication authentication , String role){

        return authentication.getAuthorities()
        .stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_"+role.toUpperCase()));
    }

    public List<String> addImages(MultipartFile[] files , int primaryKey , Products product 
        , boolean assigned
    ) throws IOException{
        Path uploadPath = Paths.get("uploads");

        if(!Files.exists(uploadPath)){
            Files.createDirectories(uploadPath);
        }

        List<String> images = new ArrayList<>();

        boolean flag;

        for(int i = 0 ; i < files.length ; i++){

            MultipartFile file = files[i];

            if(file.getSize() > 5 * 1024 * 1024){
                throw new IllegalArgumentException("File too large");
            }

            String originalName = file.getOriginalFilename();

            if(originalName == null || !originalName.contains(".")){
                throw new IllegalArgumentException("Invalid file name");
            }

            String contentType = file.getContentType();

            if(contentType == null || !contentType.startsWith("image/")){
                throw new IllegalArgumentException("File must be an image");
            }

            int dotIndex = originalName.lastIndexOf('.');

            String extension = originalName.substring(dotIndex + 1).toLowerCase();
            String ext = originalName.substring(dotIndex);

            Set<String> allowed = Set.of("png","jpg","jpeg","webp");

            if(!allowed.contains(extension)){
                throw new IllegalArgumentException("Invalid image format");
            }

            String fileName = UUID.randomUUID() + ext;

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath , StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/uploads/"+fileName;

            images.add(imageUrl);

            flag = false;

            if(!assigned && primaryKey == i){
                flag = true;
            }

            ProductImages productImages = ProductImages.builder()
                                                        .imageUrl(imageUrl)
                                                        .primary(flag)
                                                        .product(product)
                                                        .build();

            imageRepo.save(productImages);
        }
        return images;
    }

    // public List<String> uploadImage(int productId, int primaryKey, MultipartFile[] files, Authentication authentication) throws IOException {

    //     Products product = getProducts(productId);

    //     User user = getCurrentUser(authentication);

    //     if(!user.getKeyCloakId().equals(product.getSeller().getKeyCloakId())){
    //         throw new AccessDeniedException("UnAuthorized Attempt");
    //     }

    //     return addImages(files , primaryKey , product , false);
    // }

    public List<String>  uploadImage(int productId , int primaryKey, MultipartFile[] files,
    Authentication authentication
    ) throws IOException{

        boolean primaryAssigned = imageRepo.existsByProductIdAndPrimaryTrue(productId);

        Products product = getProducts(productId);

        User user = getCurrentUser(authentication);

        if(!user.getKeyCloakId().equals(product.getSeller().getKeyCloakId())){
            throw new AccessDeniedException("UnAuthorized Attempt");
        }

        return addImages(files , primaryKey , product , primaryAssigned);
        
    }


    //returning list of all products and also apply pagination
    //extract short desc by just taking 25 words of desc it has more take 25 words and then ...

    public void deleteImage(int imageId , int productId , Authentication authentication) throws IOException{

        User user = getCurrentUser(authentication);

        Products old = getProducts(productId);

        boolean isAdmin = authCheck(authentication , "admin");

        if(!isAdmin && !old.getSeller().getKeyCloakId().equals(user.getKeyCloakId())){
            throw new AccessDeniedException("Access denied");
        }

        if(!imageRepo.existsByProductIdAndId(productId, imageId)){
            throw new ImageNotFoundException("Unable to find image for the corresponding product");
        }

        ProductImages img = imageRepo.findById(imageId).orElseThrow(
            ()-> new ImageNotFoundException("Unable to find image")
        );

        Path path = Paths.get("uploads").resolve(img.getImageUrl().replace("/uploads/", ""));
        Files.deleteIfExists(path);

        imageRepo.delete(img);
    }

    public void deleteAllImage(int productId , Authentication authentication) throws IOException {

        List<ProductImages> images = imageRepo.findByProductId(productId);

        User user = getCurrentUser(authentication);

        Products old = getProducts(productId);

        boolean isAdmin = authCheck(authentication , "admin");

        if(!isAdmin && !old.getSeller().getKeyCloakId().equals(user.getKeyCloakId())){
            throw new AccessDeniedException("Access denied");
        }

        if(images.isEmpty()){
            throw new ImageNotFoundException("No images found for this product");
        }

        for(ProductImages img : images){

            Path path = Paths.get("uploads")
                    .resolve(img.getImageUrl().replace("/uploads/", ""));

            Files.deleteIfExists(path);
        }

        imageRepo.deleteByProductId(productId);
    }

    public ChangeImageGETResponse modifyImagesList(
        int productId,
        Authentication authentication) throws IOException{

            List<ProductImages> imageList = getPicOfProduct(productId);

            if(imageList.isEmpty()){
                throw new ImageNotFoundException("No Corresponding Image found");
            }

            User user = getCurrentUser(authentication);

            Products old = getProducts(productId);

            boolean isAdmin = authCheck(authentication , "admin");

            if(!isAdmin && !old.getSeller().getKeyCloakId().equals(user.getKeyCloakId())){
                throw new AccessDeniedException("Access denied");
            }
            
            List<String> imageStrings = new ArrayList<>();

            String primary = "Not Assigned";

            for(int i = 0 ; i < imageList.size() ; i++){

                imageStrings.add(imageList.get(i).getImageUrl());

                if(imageList.get(i).isPrimary()){
                    primary =  imageList.get(i).getImageUrl();
                }
            }

            return new ChangeImageGETResponse(imageStrings , imageList.size() , primary);
    }

    public void updatePrimaryImage(int productId , int oldPrimaryImage , int newPrimaryImage , Authentication authentication){

        if(oldPrimaryImage == newPrimaryImage){
            throw new IllegalArgumentException("Images cannot be same");
        }

        User user = getCurrentUser(authentication);

        ProductImages productImages1 = imageRepo.findById(oldPrimaryImage).orElseThrow(
            () -> new ProductNotFoundException("Product not found")
        );

        ProductImages productImages2 = imageRepo.findById(newPrimaryImage).orElseThrow(
            () -> new ProductNotFoundException("Product not found")
        );

        boolean isAdmin = authCheck(authentication , "admin");

        Products products = getProducts(productId);

        if(productImages1.getProduct().getId() != productId ||
        productImages2.getProduct().getId() != productId){
            throw new IllegalArgumentException("Images do not belong to this product");
        }

        if(!isAdmin &&
        !products.getSeller().getKeyCloakId().equals(user.getKeyCloakId())){
            throw new AccessDeniedException("Unauthorized Attempt");
        }

        ProductImages old = getProductImages(oldPrimaryImage);

        ProductImages newImage = getProductImages(newPrimaryImage);

        old.setPrimary(false);

        newImage.setPrimary(true);

        imageRepo.save(old);

        imageRepo.save(newImage);
    }
}