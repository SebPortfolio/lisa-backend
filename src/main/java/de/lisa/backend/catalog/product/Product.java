package de.lisa.backend.catalog.product;

import java.math.BigDecimal;
import java.util.UUID;

import de.lisa.backend.catalog.brand.Brand;
import de.lisa.backend.catalog.type.ProductType;
import de.lisa.backend.catalog.unit.Unit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Builder.Default
    @NotNull(message = "Product UUID cannot be null")
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @NotNull(message = "Product type cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @NotNull(message = "Unit cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_product_id")
    private Product parentProduct;

    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters long")
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Positive(message = "Base quantity must be positive")
    @Column(name = "base_quantity", precision = 16, scale = 6)
    private BigDecimal baseQuantity;

    @Positive(message = "Extra free quantity must be positive")
    @Column(name = "extra_free_quantity", precision = 16, scale = 6)
    private BigDecimal extraFreeQuantity;

    @Pattern(regexp = "^\\d{8,14}$", message = "GTIN must consist of 8 to 14 digits")
    @Column(name = "gtin", unique = true, length = 14)
    private String gtin;

    @Positive(message = "Deposit must be positive")
    @Column(name = "deposit", precision = 12, scale = 2)
    private BigDecimal deposit;

    @Builder.Default
    @Column(name = "is_abstract_product", nullable = false)
    private boolean isAbstractProduct = false;

    @PrePersist
    @PreUpdate
    private void trimName() {
        if (this.name != null) {
            this.name = this.name.trim();
        }
    }

    @AssertTrue(message = "The base quantity can only be null, when unit is weight based")
    private boolean isBaseQuantityOnlyNullForWeightBasedUnits() {
        if (this.baseQuantity == null) {
            return this.unit != null && this.unit.isWeightBased();
        }
        return true;
    }

    @AssertTrue(message = "A product cannot be its own parent.")
    private boolean isNotOwnParent() {
        if (this.parentProduct == null) {
            return true;
        }
        return !isSameProduct(this.parentProduct);
    }

    private boolean isSameProduct(Product other) {
        if (this == other) {
            return true;
        }
        return this.id != null && other.getId() != null && this.id.equals(other.getId());
    }

    @AssertTrue(message = "An abstract product cannot have a GTIN. Only concrete product variants can have a global article number.")
    private boolean isGtinOnlyOnConcreteProduct() {
        return this.gtin == null || !this.isAbstractProduct;
    }

}
