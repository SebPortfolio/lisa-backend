package de.lisa.backend.sales.lineitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.lisa.backend.common.field.INotNullSpecification;
import de.lisa.backend.common.numbers.IMinNumberSpecification;
import de.lisa.backend.common.numbers.IPositiveNumberSpecification;
import de.lisa.backend.sales.purchase.Purchase;
import de.lisa.backend.sales.purchase.PurchaseRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class LineItemRepositoryIT implements
        IMinNumberSpecification<LineItem>,
        IPositiveNumberSpecification<LineItem>,
        INotNullSpecification<LineItem> {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LineItemRepository lineItemRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    private static final BigDecimal DEFAULT_QUANTITY = new BigDecimal("1.0");
    private static final BigDecimal DEFAULT_PRICE_PER_PIECE = new BigDecimal("1.00");

    private Purchase defaultPurchase;
    private static final BigDecimal DEFAULT_SUB_TOTAL = new BigDecimal("3.00");

    @BeforeEach
    void persistDefaultPurchase() {
        defaultPurchase = Purchase.builder()
                .retailerUuid(UUID.randomUUID())
                .purchaseAt(OffsetDateTime.now())
                .subtotal(DEFAULT_SUB_TOTAL)
                .totalSum(DEFAULT_SUB_TOTAL)
                .build();

        defaultPurchase.addItem(getValidLineItem().build());

        purchaseRepository.saveAndFlush(defaultPurchase);
    }

    private LineItem.LineItemBuilder getValidLineItem() {
        return LineItem.builder()
                .productListingUuid(UUID.randomUUID())
                .quantity(DEFAULT_QUANTITY)
                .priceAtPurchase(DEFAULT_PRICE_PER_PIECE.multiply(DEFAULT_QUANTITY))
                .totalLinePrice(DEFAULT_PRICE_PER_PIECE.multiply(DEFAULT_QUANTITY));
    }

    @Test
    @DisplayName("should persist and find valid line item")
    public void saveAndFind_validLineItem_persistsAndFinds() {
        LineItem validItem = getValidLineItem()
                .purchase(defaultPurchase)
                .build();

        LineItem expected = lineItemRepository.saveAndFlush(validItem);

        Optional<LineItem> actual = lineItemRepository.findById(expected.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(expected.getId());
    }

    @Test
    @DisplayName("should throw exception for saving line item with priceAtPurchase smaller than discountAmount")
    public void save_priceAtPurchaseSmallerThanDiscountAmount_throwsException() {
        LineItem invalidItem = getValidLineItem()
                .quantity(new BigDecimal("1"))
                .priceAtPurchase(new BigDecimal("1.00"))
                .discountAmount(new BigDecimal("2.00"))
                .totalLinePrice(new BigDecimal("1.00"))
                .purchase(defaultPurchase)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> lineItemRepository.saveAndFlush(invalidItem));
    }

    @Test
    @DisplayName("should throw exception for saving line item with totalLinePrice not equals (quantity * priceAtPurchase - discountAmount)")
    public void save_totalLinePriceNotEqualToFormulaCalculation_throwsException() {
        LineItem invalidItem = getValidLineItem()
                .quantity(new BigDecimal("2"))
                .priceAtPurchase(new BigDecimal("2.00"))
                .discountAmount(new BigDecimal("1.00"))
                .totalLinePrice(new BigDecimal("4")) // 2 * 2 - 1 = 3
                .purchase(defaultPurchase)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> lineItemRepository.saveAndFlush(invalidItem));
    }

    // --- Einbindung der Test-Interfaces ---

    @Override
    public JpaRepository<LineItem, Long> getRepository() {
        return lineItemRepository;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Long getEntityId(LineItem entity) {
        return entity.getId();
    }

    @Override
    public LineItem buildValidEntity() {
        LineItem item = getValidLineItem().build();
        item.setPurchase(defaultPurchase);
        return item;
    }

    @Override
    public List<MinFieldRule<LineItem, ?>> getMinNumberRules() {
        return List.of(
                MinFieldRule.ofBigDecimal("priceAtPurchase", 0, LineItem::setPriceAtPurchase),
                MinFieldRule.ofBigDecimal("totalLinePrice", 0,
                        (entity, value) -> ReflectionTestUtils.setField(entity, "totalLinePrice", value)));
    }

    @Override
    public List<PositiveFieldRule<LineItem, ?>> getPositiveNumberRules() {
        return List.of(
                PositiveFieldRule.ofBigDecimal("quantity", LineItem::setQuantity),
                PositiveFieldRule.ofBigDecimal("discountAmount", LineItem::setDiscountAmount),
                PositiveFieldRule.ofBigDecimal("totalDeposit", LineItem::setTotalDeposit));
    }

    @Override
    public List<NotNullFieldRule<LineItem, ?>> getNotNullFieldRules() {
        return List.of(
                new NotNullFieldRule<>("purchase"),
                new NotNullFieldRule<>("productListingUuid"),
                new NotNullFieldRule<>("quantity"),
                new NotNullFieldRule<>("priceAtPurchase"),
                new NotNullFieldRule<>("totalLinePrice"));
    }

}
