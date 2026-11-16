package de.lisa.backend.catalog.category;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class ProductCategoryCreateService {

    private final ProductCategoryRepository repository;

    public ProductCategory createCategory(@Valid ProductCategoryCreateRequest createRequest) {

        validateUniqueName(createRequest.getName());

        ProductCategory foundParent = null;
        if (createRequest.getParentCategoryId() != null) {
            foundParent = findCategoryById(createRequest.getParentCategoryId());
        }

        ProductCategory categoryToBeCreated = buildCategory(createRequest.getName(), foundParent);
        return saveToDatabase(categoryToBeCreated);
    }

    private void validateUniqueName(String name) {
        if (findExistingCategoryByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Product category with name " + name + " allready exists");
        }
    }

    private ProductCategory findCategoryById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product category with id " + id + " not found"));
    }

    private boolean findExistingCategoryByName(String name) {
        return repository.existsByName(name);
    }

    private ProductCategory buildCategory(String name, ProductCategory parent) {
        return ProductCategory.builder()
                .name(name)
                .parentCategory(parent)
                .build();
    }

    private ProductCategory saveToDatabase(ProductCategory categoryToBeSaved) {
        log.debug("Category zum speichern: {}", categoryToBeSaved);
        return repository.save(categoryToBeSaved);
    }
}
