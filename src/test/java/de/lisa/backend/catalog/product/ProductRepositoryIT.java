package de.lisa.backend.catalog.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.lisa.backend.catalog.category.ProductCategory;
import de.lisa.backend.catalog.category.ProductCategoryRepository;
import de.lisa.backend.catalog.type.ProductType;
import de.lisa.backend.catalog.type.ProductTypeRepository;
import de.lisa.backend.catalog.unit.BaseUnitType;
import de.lisa.backend.catalog.unit.Unit;
import de.lisa.backend.catalog.unit.UnitRepository;
import de.lisa.backend.common.field.IImmutableFieldSpecification;
import de.lisa.backend.common.field.INotNullSpecification;
import de.lisa.backend.common.name.IMaxLengthNameSpecification;
import de.lisa.backend.common.name.IMinLengthNameSpecification;
import de.lisa.backend.common.name.ITrimmedNameSpecification;
import de.lisa.backend.common.numbers.IPositiveNumberSpecification;
import de.lisa.backend.common.reference.INoSelfReferenceSpecification;
import de.lisa.backend.common.uuid.IUuidGenerationSpecification;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class ProductRepositoryIT implements
        ITrimmedNameSpecification<Product>,
        IMinLengthNameSpecification<Product>,
        IMaxLengthNameSpecification<Product>,
        IUuidGenerationSpecification<Product>,
        IImmutableFieldSpecification<Product>,
        INoSelfReferenceSpecification<Product>,
        IPositiveNumberSpecification<Product>,
        INotNullSpecification<Product> {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private EntityManager entityManager;

    private ProductType defaultType;
    private ProductCategory defaultCategory;
    private Unit defaultUnit;

    private String DEFAULT_TYPE_NAME = "Type";
    private String DEFAULT_CATEGORY_NAME = "Category";
    private BaseUnitType DEFAULT_BASE_UNIT_TYPE = BaseUnitType.KILOGRAM;
    private Integer DEFAULT_COMPARISON_FACTOR = 1;

    @BeforeEach
    void persistDefaultType() {
        defaultCategory = productCategoryRepository.findByName(DEFAULT_CATEGORY_NAME)
                .orElseGet(() -> productCategoryRepository.save(
                        ProductCategory.builder()
                                .name(DEFAULT_CATEGORY_NAME)
                                .build()));

        assertThat(defaultCategory).isNotNull();

        defaultType = productTypeRepository.findByName(DEFAULT_TYPE_NAME)
                .orElseGet(() -> productTypeRepository.save(
                        ProductType.builder()
                                .name(DEFAULT_TYPE_NAME)
                                .productCategory(defaultCategory)
                                .build()));

        assertThat(defaultType).isNotNull();
    }

    @BeforeEach
    void persistUnit() {
        defaultUnit = unitRepository.findByTypeAndComparisonFactor(DEFAULT_BASE_UNIT_TYPE, DEFAULT_COMPARISON_FACTOR)
                .orElseGet(() -> unitRepository.save(
                        Unit.builder()
                                .type(DEFAULT_BASE_UNIT_TYPE)
                                .comparisonFactor(DEFAULT_COMPARISON_FACTOR)
                                .build()));

        assertThat(defaultUnit).isNotNull();
    }

    @Test
    @DisplayName("Should persists valid product with isAbstractProduct being false by default")
    void creation_nullIsAbstractProduct_defaultFalse() {
        Product product = buildValidProduct()
                .build();

        assertDoesNotThrow(() -> productRepository.saveAndFlush(product));

        entityManager.clear();

        Optional<Product> fetched = productRepository.findById(product.getId());

        assertThat(fetched).isPresent();
        assertThat(fetched.get().isAbstractProduct()).isFalse();
    }

    @Test
    @DisplayName("Should persist valid product with null brand")
    void save_validWithNullBrand_persists() {
        Product product = buildValidProduct()
                .brand(null)
                .build();

        assertDoesNotThrow(() -> productRepository.saveAndFlush(product));
    }

    @Test
    @DisplayName("Should persist valid product with null parent product")
    void save_validNullParentProduct_persists() {
        Product product = buildValidProduct()
                .parentProduct(null)
                .build();

        assertDoesNotThrow(() -> productRepository.saveAndFlush(product));
    }

    @ParameterizedTest
    @EnumSource(value = BaseUnitType.class, names = { "LITRE", "METER", "PIECE", "SHEETS" })
    @DisplayName("Should throw exception when saving with null baseQuantity and unit not weightBased (false)")
    void save_nullBaseQuantityAndUnitNotWeightBased_throwsException(BaseUnitType unitType) {
        Unit notWeightBasedUnit = unitRepository
                .findByTypeAndComparisonFactor(unitType, DEFAULT_COMPARISON_FACTOR)
                .orElseGet(() -> unitRepository.save(
                        Unit.builder()
                                .type(unitType)
                                .comparisonFactor(DEFAULT_COMPARISON_FACTOR)
                                .build()));

        Product product = buildValidProduct()
                .baseQuantity(null)
                .unit(notWeightBasedUnit)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            productRepository.saveAndFlush(product);
        });
    }

    @Test
    @DisplayName("Should persist when saving with null baseQuantity and unit is weightBased (true)")
    void save_nullBaseQuantityAndUnitIsWeightBased_persists() {
        Unit weightBasedUnit = unitRepository
                .findByTypeAndComparisonFactor(BaseUnitType.KILOGRAM, DEFAULT_COMPARISON_FACTOR)
                .orElseGet(() -> unitRepository.save(
                        Unit.builder()
                                .type(BaseUnitType.KILOGRAM)
                                .comparisonFactor(DEFAULT_COMPARISON_FACTOR)
                                .build()));

        Product product = buildValidProduct()
                .baseQuantity(null)
                .unit(weightBasedUnit)
                .build();

        assertDoesNotThrow(() -> productRepository.saveAndFlush(product));
    }

    @ParameterizedTest
    @EnumSource(BaseUnitType.class)
    @DisplayName("Should persist when saving with not null baseQuantity regardless of wheter the unit is weightBased")
    void save_notNullBaseQuantity_persists(BaseUnitType unitType) {
        Unit anyUnit = unitRepository
                .findByTypeAndComparisonFactor(unitType, DEFAULT_COMPARISON_FACTOR)
                .orElseGet(() -> unitRepository.save(
                        Unit.builder()
                                .type(unitType)
                                .comparisonFactor(DEFAULT_COMPARISON_FACTOR)
                                .build()));

        Product product = buildValidProduct()
                .baseQuantity(new BigDecimal("0.5"))
                .unit(anyUnit)
                .build();

        assertDoesNotThrow(() -> productRepository.saveAndFlush(product));
    }

    @Test
    @DisplayName("Should throw exception when saving with not null gtin and true isAbstractProduct")
    void save_notNullGtinAndTrueIsAbstractProduct_throwsException() {
        Product product = buildValidProduct()
                .gtin("12345678")
                .isAbstractProduct(true)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            productRepository.saveAndFlush(product);
        });
    }

    @Test
    @DisplayName("Should allow saving with not null gtin and false isAbstractProduct")
    void save_notNullGtinAndFalseIsAbstractProduct_persits() {
        Product product = buildValidProduct()
                .gtin("12345678")
                .isAbstractProduct(false)
                .build();

        assertDoesNotThrow(() -> productRepository.saveAndFlush(product));
    }

    @Test
    @DisplayName("Should allow saving with null gtin and true isAbstractProduct")
    void save_nullGtinAndTrueIsAbstractProduct_persists() {
        Product product = buildValidProduct()
                .gtin(null)
                .isAbstractProduct(true)
                .build();

        assertDoesNotThrow(() -> productRepository.saveAndFlush(product));
    }

    @Test
    @DisplayName("Should allow saving with null gtin and false isAbstractProduct")
    void save_nullGtinAndFalseIsAbstractProduct_persists() {
        Product product = buildValidProduct()
                .gtin(null)
                .isAbstractProduct(false)
                .build();

        assertDoesNotThrow(() -> productRepository.saveAndFlush(product));
    }

    @Test
    @DisplayName("Should allow parent when both have IDs but are different")
    void save_validParent_persistsRelation() {
        Product prod1 = productRepository.saveAndFlush(buildValidProduct().name("Prod 1").build());
        Product prod2 = productRepository.saveAndFlush(buildValidProduct().name("Prod 2").build());

        prod1.setParentProduct(prod2);
        productRepository.saveAndFlush(prod1);

        assertThat(prod1.getParentProduct().getId()).isEqualTo(prod2.getId());
    }

    @Test
    @DisplayName("Should allow parent when child is new")
    void save_transientChildWithPersistentParent_persistsRelation() {
        Product persistentParent = productRepository
                .saveAndFlush(buildValidProduct().name("Parent").build());

        Product newChild = buildValidProduct().name("New Child").build();
        newChild.setParentProduct(persistentParent);

        productRepository.saveAndFlush(newChild);

        assertThat(newChild.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should allow parent when parent is new")
    void save_persistentChildWithTransientParent_persistsRelation() {
        Product existingChild = productRepository
                .saveAndFlush(buildValidProduct().name("Existing Child").build());

        Product transientParent = buildValidProduct().name("Transient Parent").build();
        existingChild.setParentProduct(transientParent);

        // Speichern, damit Hibernate keine TransientObjectException wirft
        productRepository.save(transientParent);
        productRepository.saveAndFlush(existingChild);

        assertThat(existingChild.getParentProduct().getName()).isEqualTo("Transient Parent");
    }

    @ParameterizedTest
    @ValueSource(strings = { "1234567", "123456789012345", "12345678A", "1234-5678", "1234 5678" })
    @DisplayName("Should throw exception when saving invalid gtins")
    void save_invalidGtins_throwsException(String gtin) {
        Product product = buildValidProduct()
                .gtin(gtin)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            productRepository.saveAndFlush(product);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = { "12345678", "12345678901234", "0000567890" })
    @DisplayName("Should allow when saving valid gtins")
    void save_validGtins_persits(String gtin) {
        Product product = buildValidProduct()
                .gtin(gtin)
                .build();

        assertDoesNotThrow(() -> productRepository.saveAndFlush(product));
    }

    private Product.ProductBuilder buildValidProduct() {
        return Product.builder()
                .productType(defaultType)
                .unit(defaultUnit);
    }

    // --- Einbindung der Test-Interfaces ---

    @Override
    public JpaRepository<Product, Long> getRepository() {
        return productRepository;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Long getEntityId(Product entity) {
        return entity.getId();
    }

    @Override
    public Product buildValidEntity() {
        return buildValidProduct()
                .build();
    }

    @Override
    public UUID getUuidOfEntity(Product entity) {
        return entity.getUuid();
    }

    @Override
    public Product buildValidEntityWithName(String name) {
        return buildValidProduct()
                .name(name)
                .build();
    }

    @Override
    public String getNameOfEntity(Product entity) {
        return entity.getName();
    }

    @Override
    public int getMaxNameLength() {
        return 255;
    }

    @Override
    public int getMinNameLength() {
        return 3;
    }

    @Override
    public void setParent(Product entity, Product parent) {
        entity.setParentProduct(parent);
    }

    @Override
    public List<NotNullFieldRule<Product>> getNotNullFieldRules() {
        return List.of(
                new NotNullFieldRule<>("productType", (product, value) -> product.setProductType((ProductType) value)),
                new NotNullFieldRule<>("unit", (product, value) -> product.setUnit((Unit) value)),
                new NotNullFieldRule<>("uuid", (product, value) -> product.setUuid((UUID) value)));
    }

    @Override
    public List<ImmutableFieldRule<Product, ?>> getImmutableFieldRules() {
        return List.of(
                new ImmutableFieldRule<>(
                        Product::getUuid,
                        Product::setUuid,
                        UUID::randomUUID));
    }

    @Override
    public List<PositiveFieldRule<Product, ?>> getPositiveNumberRules() {
        return List.of(
                PositiveFieldRule.ofBigDecimal("baseQuantity", Product::setBaseQuantity),
                PositiveFieldRule.ofBigDecimal("extraFreeQuantity", Product::setExtraFreeQuantity),
                PositiveFieldRule.ofBigDecimal("deposit", Product::setDeposit));
    }
}
