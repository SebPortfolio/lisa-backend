package de.lisa.backend.pricing.price;

import java.math.BigDecimal;
import java.util.UUID;

import de.lisa.backend.common.datetimeperiod.DateTimePeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(schema = "pricing")
public class Price extends DateTimePeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Builder.Default
    @NotNull(message = "Price UUID must not be null")
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @NotNull(message = "Product listing UUID must not be null")
    @Column(name = "product_listing_uuid", nullable = false)
    private UUID productListingUuid;

    @NotNull(message = "Price type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "priceType", nullable = false, length = 20)
    private PriceType type;

    @NotNull(message = "Value per base unit must not be null")
    @Min(value = 0, message = "Price per base unit must be non-negative")
    @Column(name = "value_per_base_unit", nullable = false, precision = 16, scale = 6)
    private BigDecimal valuePerBaseUnit;

    @Min(value = 0, message = "Value per package must be non-negative")
    @Column(name = "value_per_package", precision = 16, scale = 6)
    private BigDecimal valuePerPackage;

    @Builder.Default
    @Column(name = "is_redeemed", nullable = false)
    private boolean isRedeemed = false;

    @AssertTrue(message = "Only STANDARD prices can be unlimited. Others require an ending timestamp.")
    private boolean isEndAtPresentWhenRequired() {
        if (type == null || type == PriceType.STANDARD) {
            return true;
        }
        return getEndAt() != null;
    }

}
