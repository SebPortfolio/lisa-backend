package de.lisa.backend.sales.purchase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

import de.lisa.backend.common.field.IImmutableFieldSpecification;
import de.lisa.backend.common.field.INotNullSpecification;
import de.lisa.backend.common.numbers.IPositiveNumberSpecification;
import de.lisa.backend.common.uuid.IUuidGenerationSpecification;
import de.lisa.backend.sales.lineitem.LineItem;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class PurchaseRepositoryIT implements
        IUuidGenerationSpecification<Purchase>,
        IImmutableFieldSpecification<Purchase>,
        INotNullSpecification<Purchase>,
        IPositiveNumberSpecification<Purchase> {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PurchaseRepository purchaseRepository;

    private static final BigDecimal DEFAULT_SUB_TOTAL = new BigDecimal("3.00");

    private static final BigDecimal DEFAULT_QUANTITY = new BigDecimal("1.0");
    private static final BigDecimal DEFAULT_PRICE_PER_PIECE = new BigDecimal("1.00");

    @Test
    @DisplayName("should throw exceptionen when persisting with an empty line item list")
    public void save_emptyLineItems_throwsException() {
        Purchase empty = getValidPurchase();
        ReflectionTestUtils.setField(empty, "items", new ArrayList<>());

        assertThrows(ConstraintViolationException.class, () -> purchaseRepository.saveAndFlush(empty));
    }

    @Test
    @DisplayName("should add another item to item list, connect item with this purchase and recalculate")
    public void addItem_expandListConnectItemToPurchaseAndRecalculate_persistsAndFinds() {
        Purchase two = getValidPurchase();
        LineItem second = getValidLineItemBuilder()
                .priceAtPurchase(new BigDecimal("0.50"))
                .totalLinePrice(new BigDecimal("0.50"))
                .build();

        two.addItem(second);

        assertThat(two.getSubtotal()).isEqualByComparingTo("1.50");

        Purchase expected = purchaseRepository.save(two);

        Optional<Purchase> actual = purchaseRepository.findById(expected.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getItems()).hasSize(2);
        assertThat(actual.get().getItems().get(1).getProductListingUuid())
                .isEqualByComparingTo(second.getProductListingUuid());
    }

    @Test
    @DisplayName("should throw exception when totalSum calculation is not equal to (subtotal - basketDiscountAmount + totalDepositBuy - totalDepositReturn - redeemedCredit")
    public void save_totalSum_notEqualsCalculation() {
        Purchase wrongCalculation = getValidPurchase();

        ReflectionTestUtils.setField(wrongCalculation, "totalSum", new BigDecimal("100.0"));

        assertThrows(DataIntegrityViolationException.class, () -> purchaseRepository.saveAndFlush(wrongCalculation));
    }

    @Test
    @DisplayName("should calculate totalSum correctly with basket discount, deposits and redeemed credit")
    public void addItem_calculatesTotalSumCorrectlyWithBasketDiscountDepositsAndRedeemedCredit_works() {
        Purchase correctCalculation = getValidPurchase();
        correctCalculation.setBasketDiscountAmount(new BigDecimal("0.20"));
        correctCalculation.setTotalDepositReturn(new BigDecimal("0.50"));
        correctCalculation.setRedeemedCredit(new BigDecimal("0.10"));

        LineItem itemToAdd = correctCalculation.getItems().get(0);
        itemToAdd.setTotalDeposit(new BigDecimal("0.25"));
        ReflectionTestUtils.setField(correctCalculation, "items", new ArrayList<>());

        correctCalculation.addItem(itemToAdd);

        BigDecimal expected = new BigDecimal("0.45"); // 1 - 0.2 + 0.25 - 0.5 - 0.1

        assertThat(correctCalculation.getTotalSum()).isEqualByComparingTo(expected);
    }

    private Purchase getValidPurchase() {
        LineItem item = getValidLineItemBuilder()
                .build();

        Purchase valid = Purchase.builder()
                .retailerUuid(UUID.randomUUID())
                .purchaseAt(OffsetDateTime.now())
                .subtotal(DEFAULT_SUB_TOTAL)
                .totalSum(DEFAULT_SUB_TOTAL)
                .build();

        valid.addItem(item);

        return valid;
    }

    private LineItem.LineItemBuilder getValidLineItemBuilder() {
        return LineItem.builder()
                .productListingUuid(UUID.randomUUID())
                .quantity(DEFAULT_QUANTITY)
                .priceAtPurchase(DEFAULT_PRICE_PER_PIECE.multiply(DEFAULT_QUANTITY))
                .totalLinePrice(DEFAULT_PRICE_PER_PIECE.multiply(DEFAULT_QUANTITY));
    }

    // --- Einbindung der Test-Interfaces ---

    @Override
    public JpaRepository<Purchase, Long> getRepository() {
        return purchaseRepository;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Long getEntityId(Purchase entity) {
        return entity.getId();
    }

    @Override
    public Purchase buildValidEntity() {
        return getValidPurchase();
    }

    @Override
    public UUID getUuidOfEntity(Purchase entity) {
        return entity.getUuid();
    }

    @Override
    public List<ImmutableFieldRule<Purchase, ?>> getImmutableFieldRules() {
        return List.of(
                new ImmutableFieldRule<>("uuid", Purchase::getUuid, Purchase::setUuid, UUID::randomUUID),
                new ImmutableFieldRule<>("retailerUuid", Purchase::getRetailerUuid, Purchase::setRetailerUuid,
                        UUID::randomUUID),
                new ImmutableFieldRule<>("purchaseAt", Purchase::getPurchaseAt, Purchase::setPurchaseAt,
                        () -> OffsetDateTime.now().plusDays(2)));
    }

    @Override
    public List<NotNullFieldRule<Purchase, ?>> getNotNullFieldRules() {
        return List.of(
                new NotNullFieldRule<>("uuid"),
                new NotNullFieldRule<>("retailerUuid"),
                new NotNullFieldRule<>("purchaseAt"),
                new NotNullFieldRule<>("subtotal"),
                new NotNullFieldRule<>("totalSum"));
    }

    @Override
    public List<PositiveFieldRule<Purchase, ?>> getPositiveNumberRules() {
        return List.of(
                PositiveFieldRule.ofBigDecimal("totalDepositBuy",
                        (entity, value) -> ReflectionTestUtils.setField(entity, "totalDepositBuy", value)),
                PositiveFieldRule.ofBigDecimal("totalDepositReturn", Purchase::setTotalDepositReturn),
                PositiveFieldRule.ofBigDecimal("basketDiscountAmount", Purchase::setBasketDiscountAmount),
                PositiveFieldRule.ofBigDecimal("redeemedCredit", Purchase::setRedeemedCredit));
    }
}
