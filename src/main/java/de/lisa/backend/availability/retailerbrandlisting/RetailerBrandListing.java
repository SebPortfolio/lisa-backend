package de.lisa.backend.availability.retailerbrandlisting;

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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(schema = "availability")
public class RetailerBrandListing extends DateTimePeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NonNull
    @NotNull(message = "Retailer must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retailer_id", nullable = false)
    private Retailer retailer;

    @NonNull
    @NotNull(message = "Brand UUID must not be null")
    @Column(name = "brand_uuid", nullable = false)
    private UUID brandUuid;
}
