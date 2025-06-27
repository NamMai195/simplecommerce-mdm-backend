package com.simplecommerce_mdm.category.mapper;

import com.simplecommerce_mdm.category.dto.CategoryRequest;
import com.simplecommerce_mdm.category.dto.CategoryResponse;
import com.simplecommerce_mdm.category.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    /**
     * Converts a CategoryRequest DTO to a Category entity.
     * Note: This method does not handle setting the parent category or slug,
     * as that requires business logic (database lookups, slug generation).
     *
     * @param request The CategoryRequest DTO.
     * @return A new Category entity.
     */
    public Category toCategory(CategoryRequest request) {
        if (request == null) {
            return null;
        }
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    /**
     * Converts a Category entity to a CategoryResponse DTO.
     *
     * @param category The Category entity.
     * @return A new CategoryResponse DTO.
     */
    public CategoryResponse toCategoryResponse(Category category) {
        if (category == null) {
            return null;
        }

        Integer parentId = (category.getParent() != null) ? category.getParent().getId() : null;

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(parentId)
                .build();
    }
}