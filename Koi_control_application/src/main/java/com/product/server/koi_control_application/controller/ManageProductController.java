package com.product.server.koi_control_application.controller;

import com.product.server.koi_control_application.model.Product;
import com.product.server.koi_control_application.pojo.BaseResponse;
import com.product.server.koi_control_application.service_interface.IImageService;
import com.product.server.koi_control_application.service_interface.IProductService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/manage/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RolesAllowed({"ROLE_ADMIN"})
public class ManageProductController {
    private final IProductService productService;
    private final IImageService imageService;

    @PostMapping
    public ResponseEntity<BaseResponse> createProduct(@RequestPart("product") Product product, @RequestParam("image") MultipartFile file) throws IOException {
        String filename = imageService.uploadImage(file);
        product.setImageUrl(filename);
        Product createdProduct = productService.createProduct(product);
        BaseResponse response = BaseResponse.builder()
                .data(createdProduct)
                .statusCode(HttpStatus.CREATED.value())
                .message("Create product success")
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

//    @PostMapping("/{productId}/image/upload/")
//    public ResponseEntity<BaseResponse> uploadProductImage(@PathVariable int productId, @RequestParam("image") MultipartFile file) throws IOException {
//        String filename = imageService.uploadImage(file);
//        Product savedProduct = productService.getProduct(productId);
//        savedProduct.setImageUrl(filename);
//        productService.createProduct(savedProduct);
//        BaseResponse response = BaseResponse.builder()
//                .data(savedProduct)
//                .statusCode(HttpStatus.CREATED.value())
//                .message("Upload image success to "+ savedProduct.getName())
//                .build();
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        BaseResponse response = BaseResponse.builder()
                .data(null)
                .statusCode(HttpStatus.OK.value())
                .message("Product deleted successfully")
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
