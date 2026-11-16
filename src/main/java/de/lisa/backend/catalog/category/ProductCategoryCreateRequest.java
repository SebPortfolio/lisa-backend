package de.lisa.backend.catalog.category;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ProductCategoryCreateRequest {

    private String name;
    private Long parentCategoryId;
}
