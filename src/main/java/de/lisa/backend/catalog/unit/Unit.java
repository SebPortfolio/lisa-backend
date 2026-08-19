package de.lisa.backend.catalog.unit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Unit type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, length = 10)
    private BaseUnitType type;

    @NotNull(message = "Comparison factor must not be null")
    @Positive(message = "Comparison factor must be greater than zero")
    @Max(value = 9999, message = "Factor must be up to 4 digits")
    @Column(name = "comparison_factor", nullable = false)
    private Integer comparisonFactor;
    // Ein Produkt kann preislich auf 100 Stück, statt 1 Stück gerechnet werden.
    // Dann ist der Typ "Stück" und der Faktor "100".

    public boolean isWeightBased() {
        return this.type.isWeightBased();
    }

}
