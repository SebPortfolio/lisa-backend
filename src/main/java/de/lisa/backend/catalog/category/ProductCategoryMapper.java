package de.lisa.backend.catalog.category;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import de.sebportfolio.lisa.catalog.model.ProductCategoryCreateRequestWdto;
import de.sebportfolio.lisa.catalog.model.ProductCategoryWdto;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {
    @Mapping(target = "parent", source = "parentCategory")
    ProductCategoryWdto toWdto(ProductCategory entity);

    @Mapping(target = "parentCategory", source = "parent")
    ProductCategory fromWdto(ProductCategoryWdto wdto);

    List<ProductCategoryWdto> toWdtoList(List<ProductCategory> entities);

    List<ProductCategory> fromWdtoList(List<ProductCategoryWdto> wdtos);

    @Mapping(target = "parentCategoryId", source = "parentId")
    ProductCategoryCreateRequest fromCreateWdto(ProductCategoryCreateRequestWdto wdto);
}
