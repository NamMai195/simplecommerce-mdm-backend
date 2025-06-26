package com.simplecommerce_mdm.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Category name must be less than 255 characters")
    private String name;

    private String description;
    
    // Assuming parentId can be null for top-level categories
    private Integer parentId; 
} 