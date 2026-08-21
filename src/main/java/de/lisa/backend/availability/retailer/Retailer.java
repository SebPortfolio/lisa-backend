package de.lisa.backend.availability.retailer;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
public class Retailer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Builder.Default
    @NotNull(message = "Retailer UUID cannot be null")
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @NotBlank(message = "Retailer name cannot be blank")
    @Size(min = 2, max = 50, message = "Retailer name must be between 2 and 50 characters long")
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    @PrePersist
    @PreUpdate
    private void trimName() {
        if (this.name != null) {
            this.name = this.name.trim();
        }
    }
}