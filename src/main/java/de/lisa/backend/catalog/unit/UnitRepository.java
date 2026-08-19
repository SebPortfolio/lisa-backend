package de.lisa.backend.catalog.unit;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    Optional<Unit> findByTypeAndComparisonFactor(BaseUnitType type, Integer comparisonFactor);
}
