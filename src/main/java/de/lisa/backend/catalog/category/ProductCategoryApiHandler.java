package de.lisa.backend.catalog.category;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import de.sebportfolio.lisa.catalog.api.ProductCategoryApi;
import de.sebportfolio.lisa.catalog.model.ProductCategoryCreateRequestWdto;
import de.sebportfolio.lisa.catalog.model.ProductCategoryWdto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductCategoryApiHandler implements ProductCategoryApi {

    private final ProductCategoryService categoryService;
    private final ProductCategoryCreateService categoryCreateService;
    private final ProductCategoryMapper mapper;

    @Override
    public ResponseEntity<List<ProductCategoryWdto>> getCategoryList() {
        List<ProductCategory> list = categoryService.getCategoryList();
        List<ProductCategoryWdto> wdtos = mapper.toWdtoList(list);
        return ResponseEntity.ok(wdtos);
    }

    @Override
    public ResponseEntity<ProductCategoryWdto> createCategory(
            ProductCategoryCreateRequestWdto productCategoryCreateRequestWdto) {
        log.debug("Request angekommen: {}", productCategoryCreateRequestWdto);
        ProductCategoryCreateRequest createRequest = mapper.fromCreateWdto(productCategoryCreateRequestWdto);
        ProductCategory createdCategory = categoryCreateService.createCategory(createRequest);
        ProductCategoryWdto wdto = mapper.toWdto(createdCategory);
        return ResponseEntity.ok(wdto);
    }

}
