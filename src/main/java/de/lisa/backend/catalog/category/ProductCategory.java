package de.lisa.backend.catalog.category;

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
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Table(schema = "catalog")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NonNull
    @NotBlank(message = "Product category name must not be blank")
    @Size(min = 3, max = 255, message = "Product category name must be between 3 and 255 characters long")
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_product_category_id")
    private ProductCategory parentCategory;

    @PrePersist
    @PreUpdate
    private void trimName() {
        if (this.name != null) {
            this.name = this.name.trim();
        }
    }

    @AssertTrue(message = "A category cannot be its own parent.")
    private boolean isNotOwnParent() {
        if (this.parentCategory == null) {
            return true;
        }
        return !isSameCategory(this.parentCategory);
    }

    private boolean isSameCategory(ProductCategory other) {
        if (this == other) {
            return true;
        }
        return this.id != null && other.getId() != null && this.id.equals(other.getId());
    }

}
