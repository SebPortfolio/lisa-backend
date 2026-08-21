package de.lisa.backend.availability.retailer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RetailerRepository extends JpaRepository<Retailer, Long> {

    Optional<Retailer> findByName(String name);
}
