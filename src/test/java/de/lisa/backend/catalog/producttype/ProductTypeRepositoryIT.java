package de.lisa.backend.catalog.producttype;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.lisa.backend.catalog.category.ProductCategory;
import de.lisa.backend.catalog.category.ProductCategoryRepository;
import de.lisa.backend.catalog.type.ProductType;
import de.lisa.backend.catalog.type.ProductTypeRepository;
import de.lisa.backend.common.field.INotNullSpecification;
import de.lisa.backend.common.name.IMaxLengthNameSpecification;
import de.lisa.backend.common.name.IMinLengthNameSpecification;
import de.lisa.backend.common.name.INotBlankNameSpecification;
import de.lisa.backend.common.name.IUniqueNameSpecification;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class ProductTypeRepositoryIT implements
        IMinLengthNameSpecification<ProductType>,
        IMaxLengthNameSpecification<ProductType>,
        INotBlankNameSpecification<ProductType>,
        IUniqueNameSpecification<ProductType>,
        INotNullSpecification<ProductType> {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private EntityManager entityManager;

    private ProductCategory defaultCategory;

    @BeforeEach
    private void persistDefaultCategory() {
        defaultCategory = productCategoryRepository.findByName("Category")
                .orElseGet(() -> productCategoryRepository.save(
                        ProductCategory.builder()
                                .name("Category")
                                .build()));
    }

    @Test
    @DisplayName("Should successfully find an existing type by its name")
    void findByName_withExistingName_returnsProductType() {
        ProductType expected = getValidType().build();

        productTypeRepository.saveAndFlush(expected);

        entityManager.clear();

        Optional<ProductType> actual = productTypeRepository.findByName(expected.getName());

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(expected.getId());
        assertThat(actual.get().getName()).isEqualTo(expected.getName());
    }

    @Test
    @DisplayName("Should return empty optional when type name does not exist")
    void findByName_whenProductTypeDoesNotExist_returnsEmptyOptional() {
        Optional<ProductType> actual = productTypeRepository.findByName("NonExistingName");
        assertThat(actual).isEmpty();
    }

    private ProductType.ProductTypeBuilder getValidType() {
        return ProductType.builder()
                .name("Type_" + UUID.randomUUID())
                .productCategory(defaultCategory);
    }

    // --- Einbindung der Test-Interfaces ---

    @Override
    public JpaRepository<ProductType, Long> getRepository() {
        return productTypeRepository;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public ProductType buildValidEntityWithName(String name) {
        return getValidType()
                .name(name)
                .build();
    }

    @Override
    public String getNameOfEntity(ProductType entity) {
        return entity.getName();
    }

    @Override
    public Long getEntityId(ProductType entity) {
        return entity.getId();
    }

    @Override
    public int getMinNameLength() {
        return 3;
    }

    @Override
    public int getMaxNameLength() {
        return 100;
    }

    @Override
    public ProductType buildValidEntity() {
        return getValidType().build();
    }

    @Override
    public List<NotNullFieldRule<ProductType>> getNotNullFieldRules() {
        return List.of(
                new NotNullFieldRule<>("productCategory",
                        (type, value) -> type.setProductCategory((ProductCategory) value)));
    }
}
