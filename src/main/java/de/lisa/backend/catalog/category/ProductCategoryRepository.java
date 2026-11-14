package de.lisa.backend.catalog.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    Optional<ProductCategory> findByName(String name);

    @Query("SELECT c FROM ProductCategory c LEFT JOIN FETCH c.parentCategory")
    List<ProductCategory> findAllWithParent();

    boolean existsByName(String name);

}
