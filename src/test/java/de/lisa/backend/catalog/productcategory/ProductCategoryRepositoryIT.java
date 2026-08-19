package de.lisa.backend.catalog.productcategory;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;

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
import de.lisa.backend.common.name.IMaxLengthNameSpecification;
import de.lisa.backend.common.name.IMinLengthNameSpecification;
import de.lisa.backend.common.name.INotBlankNameSpecification;
import de.lisa.backend.common.name.IUniqueNameSpecification;
import de.lisa.backend.common.reference.INoSelfReferenceSpecification;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class ProductCategoryRepositoryIT implements
        IMinLengthNameSpecification<ProductCategory>,
        IMaxLengthNameSpecification<ProductCategory>,
        INotBlankNameSpecification<ProductCategory>,
        IUniqueNameSpecification<ProductCategory>,
        INoSelfReferenceSpecification<ProductCategory> {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private EntityManager entityManager;

    private static final String DEFAULT_NAME = "Category";

    @Test
    @DisplayName("Should allow parent when both have IDs but are different")
    void save_validParent_persistsRelation() {
        ProductCategory cat1 = productCategoryRepository.saveAndFlush(getValidCategory().name("Cat 1").build());
        ProductCategory cat2 = productCategoryRepository.saveAndFlush(getValidCategory().name("Cat 2").build());

        cat1.setParentCategory(cat2);
        productCategoryRepository.saveAndFlush(cat1);

        assertThat(cat1.getParentCategory().getId()).isEqualTo(cat2.getId());
    }

    @Test
    @DisplayName("Should allow parent when child is new")
    void save_transientChildWithPersistentParent_persistsRelation() {
        ProductCategory persistentParent = productCategoryRepository
                .saveAndFlush(getValidCategory().name("Parent").build());

        ProductCategory newChild = getValidCategory().name("New Child").build();
        newChild.setParentCategory(persistentParent);

        productCategoryRepository.saveAndFlush(newChild);

        assertThat(newChild.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should allow parent when parent is new")
    void save_persistentChildWithTransientParent_persistsRelation() {
        ProductCategory existingChild = productCategoryRepository
                .saveAndFlush(getValidCategory().name("Existing Child").build());

        ProductCategory transientParent = getValidCategory().name("Transient Parent").build();
        existingChild.setParentCategory(transientParent);

        // Speichern, damit Hibernate keine TransientObjectException wirft
        productCategoryRepository.save(transientParent);
        productCategoryRepository.saveAndFlush(existingChild);

        assertThat(existingChild.getParentCategory().getName()).isEqualTo("Transient Parent");
    }

    @Test
    @DisplayName("Should successfully find an existing category by its name")
    void findByName_withExistingName_returnsProductCategory() {
        ProductCategory expected = getValidCategory().build();

        productCategoryRepository.saveAndFlush(expected);

        entityManager.clear();

        Optional<ProductCategory> actual = productCategoryRepository.findByName(expected.getName());

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(expected.getId());
        assertThat(actual.get().getName()).isEqualTo(expected.getName());
    }

    @Test
    @DisplayName("Should return empty optional when category name does not exist")
    void findByName_whenProductCategoryDoesNotExist_returnsEmptyOptional() {
        Optional<ProductCategory> actual = productCategoryRepository.findByName("NonExistingName");
        assertThat(actual).isEmpty();
    }

    private ProductCategory.ProductCategoryBuilder getValidCategory() {
        return ProductCategory.builder()
                .name(DEFAULT_NAME);
    }

    // --- Einbindung der Test-Interfaces ---

    @Override
    public JpaRepository<ProductCategory, Long> getRepository() {
        return productCategoryRepository;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public ProductCategory buildValidEntityWithName(String name) {
        return getValidCategory()
                .name(name)
                .build();
    }

    @Override
    public String getNameOfEntity(ProductCategory entity) {
        return entity.getName();
    }

    @Override
    public Long getEntityId(ProductCategory entity) {
        return entity.getId();
    }

    @Override
    public int getMinNameLength() {
        return 3;
    }

    @Override
    public int getMaxNameLength() {
        return 255;
    }

    @Override
    public ProductCategory buildValidEntity() {
        return getValidCategory()
                .build();
    }

    @Override
    public void setParent(ProductCategory entity, ProductCategory parent) {
        entity.setParentCategory(parent);
    }
}
