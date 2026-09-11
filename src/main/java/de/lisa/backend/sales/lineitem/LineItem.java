package de.lisa.backend.sales.lineitem;

import java.math.BigDecimal;
import java.util.UUID;

import de.lisa.backend.sales.purchase.Purchase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
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
public class LineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Purchase must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @NonNull
    @NotNull(message = "Product listing UUID must not be null")
    @Column(name = "product_listing_uuid", nullable = false)
    private UUID productListingUuid;

    @NonNull
    @NotNull(message = "Quantity must not be null")
    @Positive(message = "Quantity must be positive")
    @Column(name = "quantity", nullable = false, precision = 12, scale = 4)
    @Setter(AccessLevel.NONE)
    private BigDecimal quantity; // z.B. 0,250 (kg) oder 3 (Stück)

    @NonNull
    @NotNull(message = "Price at purchase must not be null")
    @Min(value = 0, message = "Price at purchase must be non-negative")
    @Column(name = "price_at_purchase", nullable = false, precision = 12, scale = 2)
    @Setter(AccessLevel.NONE)
    private BigDecimal priceAtPurchase;

    @Positive(message = "Discount amount must be positive")
    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Setter(AccessLevel.NONE)
    private BigDecimal discountAmount;

    @Positive(message = "Total deposit must be positive")
    @Column(name = "total_deposit", precision = 12, scale = 2)
    private BigDecimal totalDeposit;

    @NonNull
    @NotNull(message = "Total line price must not be null")
    @Min(value = 0, message = "Total line price must be non-negative")
    @Column(name = "total_line_price", nullable = false, precision = 12, scale = 2)
    @Setter(AccessLevel.NONE)
    private BigDecimal totalLinePrice;

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        recalculateLinePrice();
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
        recalculateLinePrice();
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
        recalculateLinePrice();
    }

    private void recalculateLinePrice() {
        totalLinePrice = quantity.multiply(priceAtPurchase).subtract(nullToZero(discountAmount));
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
