package de.lisa.backend.pricing.price;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.lisa.backend.common.datetimeperiod.IDateTimePeriodSpecification;
import de.lisa.backend.common.field.IImmutableFieldSpecification;
import de.lisa.backend.common.field.INotNullSpecification;
import de.lisa.backend.common.numbers.IMinNumberSpecification;
import de.lisa.backend.common.uuid.IUuidGenerationSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class PriceRepositoryIT implements IUuidGenerationSpecification<Price>,
        IDateTimePeriodSpecification<Price>,
        IMinNumberSpecification<Price>,
        INotNullSpecification<Price>,
        IImmutableFieldSpecification<Price> {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private EntityManager entityManager;

    private static final OffsetDateTime DEFAULT_START = OffsetDateTime.of(2026, 5, 1, 0, 0, 0, 0,
            OffsetDateTime.now().getOffset());
    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("2.00");

    @ParameterizedTest
    @EnumSource(value = PriceType.class, names = { "OFFER", "APP_OFFER", "APP_COUPON" })
    @DisplayName("Should throw exception when non-STANDARD price types has no endAt (DB Constraint)")
    void save_nonStandardTypeWithoutEndAt_throwsException(PriceType type) {
        Price price = getValidPrice()
                .type(type)
                .endAt(null)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            priceRepository.saveAndFlush(price);
        });
    }

    @Test
    @DisplayName("Should allow STANDARD price type with null endAt")
    void save_standardTypeWithoutEndAt_works() {
        Price price = getValidPrice().build();

        assertDoesNotThrow(() -> {
            priceRepository.saveAndFlush(price);
        });
    }

    @Test
    @DisplayName("Should throw exception when trying to save more than one price with STANDARD price type and NULL endAt for same product listing (DB Constraint)")
    void save_duplicateStandardInfinitePrice_throwsException() {
        Price price1 = getValidPrice().build();
        priceRepository.saveAndFlush(price1);

        Price price2 = getValidPrice()
                .productListingUuid(price1.getProductListingUuid())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            priceRepository.saveAndFlush(price2);
        });
    }

    private Price.PriceBuilder<?, ?> getValidPrice() {
        return Price.builder()
                .productListingUuid(UUID.randomUUID())
                .type(PriceType.STANDARD)
                .valuePerBaseUnit(DEFAULT_PRICE)
                .startAt(DEFAULT_START);
    }

    // --- Einbindung der Test-Interfaces ---

    @Override
    public JpaRepository<Price, Long> getRepository() {
        return priceRepository;
    }

    @Override
    public Price buildValidEntity() {
        return getValidPrice().build();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Long getEntityId(Price entity) {
        return entity.getId();
    }

    @Override
    public List<MinFieldRule<Price, ?>> getMinNumberRules() {
        return List.of(
                MinFieldRule.ofBigDecimal("valuePerBaseUnit", 0, Price::setValuePerBaseUnit),
                MinFieldRule.ofBigDecimal("pricePerPackage", 0, Price::setValuePerPackage));
    }

    @Override
    public UUID getUuidOfEntity(Price entity) {
        return entity.getUuid();
    }

    @Override
    public List<NotNullFieldRule<Price>> getNotNullFieldRules() {
        return List.of(new NotNullFieldRule<>("uuid", (price, value) -> price.setUuid((UUID) value)),
                new NotNullFieldRule<>("productListingUuid",
                        (price, value) -> price.setProductListingUuid((UUID) value)),
                new NotNullFieldRule<>("type", (price, value) -> price.setType((PriceType) value)),
                new NotNullFieldRule<>("valuePerBaseUnit",
                        (price, value) -> price.setValuePerBaseUnit((BigDecimal) value)));
    }

    @Override
    public List<ImmutableFieldRule<Price, ?>> getImmutableFieldRules() {
        return List.of(
                new ImmutableFieldRule<>(Price::getUuid, Price::setUuid, UUID::randomUUID));
    }

}