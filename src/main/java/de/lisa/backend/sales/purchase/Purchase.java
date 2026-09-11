package de.lisa.backend.sales.purchase;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.lisa.backend.sales.lineitem.LineItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(schema = "sales")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Builder.Default
    @NonNull
    @NotNull(message = "Purchase UUID must not be null")
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @NonNull
    @NotNull(message = "Retailer UUID must not be null")
    @Column(name = "retailer_uuid", nullable = false, updatable = false)
    private UUID retailerUuid;

    @NonNull
    @NotNull(message = "Purchase timestamp must not be null")
    @Column(name = "purchase_at", nullable = false, updatable = false)
    private OffsetDateTime purchaseAt;

    @Builder.Default
    @NonNull
    @NotEmpty(message = "Purchase must contain at least one line item")
    @Valid
    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<LineItem> items = new ArrayList<>();

    @NonNull
    @NotNull(message = "Subtotal must not be null")
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    @Setter(AccessLevel.NONE)
    private BigDecimal subtotal;

    @Positive(message = "Total deposit buy must be positive")
    @Column(name = "total_deposit_buy", precision = 12, scale = 2)
    @Setter(AccessLevel.NONE)
    private BigDecimal totalDepositBuy;

    @Positive(message = "Total deposit return must be positive")
    @Column(name = "total_deposit_return", precision = 12, scale = 2)
    private BigDecimal totalDepositReturn;

    @Positive(message = "Basket discount amount must be positive")
    @Column(name = "basket_discount_amount", precision = 12, scale = 2)
    private BigDecimal basketDiscountAmount;

    @Positive(message = "Redeemed credit must be positive")
    @Column(name = "redeemed_credit", precision = 12, scale = 2)
    private BigDecimal redeemedCredit;

    @NonNull
    @NotNull(message = "Total sum must not be null")
    @Column(name = "total_sum", nullable = false, precision = 12, scale = 2)
    @Setter(AccessLevel.NONE)
    private BigDecimal totalSum;

    public void addItem(LineItem item) {
        items.add(item);
        item.setPurchase(this);

        recalculateTotals();
    }

    private void recalculateTotals() {
        subtotal = items.stream()
                .map(LineItem::getTotalLinePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal calculatedDeposit = items.stream()
                .map(LineItem::getTotalDeposit)
                .map(this::nullToZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalDepositBuy = calculatedDeposit.compareTo(BigDecimal.ZERO) > 0
                ? calculatedDeposit
                : null;

        totalSum = subtotal
                .subtract(nullToZero(basketDiscountAmount))
                .add(nullToZero(totalDepositBuy))
                .subtract(nullToZero(totalDepositReturn))
                .subtract(nullToZero(redeemedCredit));
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
