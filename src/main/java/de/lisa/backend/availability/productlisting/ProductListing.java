package de.lisa.backend.availability.productlisting;

import java.util.UUID;

import de.lisa.backend.availability.retailer.Retailer;
import de.lisa.backend.common.datetimeperiod.DateTimePeriod;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
@Table(schema = "availability")
public class ProductListing extends DateTimePeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Builder.Default
    @NotNull(message = "Product listing UUID must not be null")
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @NotNull(message = "Product UUID must not be null")
    @Column(name = "product_uuid", nullable = false)
    private UUID productUuid;

    @NotNull(message = "Retailer must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retailer_id", nullable = false)
    private Retailer retailer;

    @Size(min = 8, max = 16, message = "SKU must be between 8 and 16 characters long")
    @Column(name = "sku", length = 16)
    private String sku; // Stock Keeping Unit (Händlerspezifische Artikelnummer)

    @Column(name = "current_price_uuid")
    private UUID currentPriceUuid; // Optionaler Performance Pointer auf die aktuelle Preis-Entität.
                                   // Inkonistenzgefahr !

    @PrePersist
    @PreUpdate
    private void trimSku() {
        if (this.sku != null) {
            this.sku = this.sku.trim();
        }
    }
}
