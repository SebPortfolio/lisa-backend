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
import de.lisa.backend.common.string.IMaxLengthStringSpecification;
import de.lisa.backend.common.string.IMinLengthStringSpecification;
import de.lisa.backend.common.string.INotBlankStringSpecification;
import de.lisa.backend.common.string.IUniqueStringSpecification;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class ProductTypeRepositoryIT implements
        IMinLengthStringSpecification<ProductType>,
        IMaxLengthStringSpecification<ProductType>,
        INotBlankStringSpecification<ProductType>,
        IUniqueStringSpecification<ProductType>,
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
    public Long getEntityId(ProductType entity) {
        return entity.getId();
    }

    @Override
    public ProductType buildValidEntity() {
        return getValidType().build();
    }

    @Override
    public List<NotNullFieldRule<ProductType, ?>> getNotNullFieldRules() {
        return List.of(
                new NotNullFieldRule<>("productCategory"));
    }

    @Override
    public List<TrimmedFieldRule<ProductType>> getTrimmedFieldRules() {
        return List.of(
                new TrimmedFieldRule<>("name", ProductType::getName, ProductType::setName));
    }

    @Override
    public List<UniqueFieldRule<ProductType>> getUniqueFieldRules() {
        return List.of(
                new UniqueFieldRule<>("name", ProductType::getName, ProductType::setName));
    }

    @Override
    public List<NotBlankFieldRule<ProductType>> getNotBlankFieldRules() {
        return List.of(
                new NotBlankFieldRule<>("name", ProductType::getName, ProductType::setName));
    }

    @Override
    public List<MaxLengthFieldRule<ProductType>> getMaxLengthFieldRules() {
        return List.of(
                new MaxLengthFieldRule<>("name", 100, ProductType::getName, ProductType::setName));
    }

    @Override
    public List<MinLengthFieldRule<ProductType>> getMinLengthFieldRules() {
        return List.of(
                new MinLengthFieldRule<>("name", 3, ProductType::getName, ProductType::setName));
    }
}
