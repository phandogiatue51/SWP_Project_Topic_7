package com.product.server.koi_control_application.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.product.server.koi_control_application.pojo.SlugGenerator;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Hidden
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must be less than 100 characters")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private int price;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;


    @Column(name = "image_url")
    private String imageUrl;

    @PositiveOrZero(message = "Stock must be zero or positive")
    private Integer stock;

    @Column(name = "category_id")
    private int categoryId;

    private String slug;

    private boolean disabled;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = SlugGenerator.toSlug(this.name);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"product", "feedbacks"})
    private List<Feedback> feedbacks = new ArrayList<>();

    @Column(name = "average_rating")
    private Double averageRating;

    public void calculateAverageRating() {
        if (feedbacks.isEmpty()) {
            this.averageRating = 0.0;
        } else {
            double sum = feedbacks.stream().mapToInt(Feedback::getRating).sum();
            this.averageRating = sum / feedbacks.size();
        }
    }

    public Product decreaseStock(int amount) {
        this.stock = this.stock - amount;
        return this;
    }

    public Product increaseStock(int amount) {
        this.stock = this.stock + amount;
        return this;
    }


}
