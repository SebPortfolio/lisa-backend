package de.lisa.backend.availability.productlisting;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.lisa.backend.availability.retailer.Retailer;
import de.lisa.backend.availability.retailer.RetailerRepository;
import de.lisa.backend.common.field.IImmutableFieldSpecification;
import de.lisa.backend.common.field.INotNullSpecification;
import de.lisa.backend.common.string.ITrimmedStringSpecification;
import de.lisa.backend.common.uuid.IUuidGenerationSpecification;
import jakarta.persistence.EntityManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class ProductListingRepositoryIT implements
        IUuidGenerationSpecification<ProductListing>,
        IImmutableFieldSpecification<ProductListing>,
        INotNullSpecification<ProductListing>,
        ITrimmedStringSpecification<ProductListing> {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProductListingRepository productListingRepository;

    @Autowired
    private RetailerRepository retailerRepository;

    private static final String DEFAULT_NAME = "Retailer";
    private Retailer defaultRetailer;

    @BeforeEach
    void persistDefaultRetailer() {
        defaultRetailer = retailerRepository.findByName(DEFAULT_NAME)
                .orElseGet(() -> retailerRepository.save(
                        Retailer.builder()
                                .name(DEFAULT_NAME)
                                .build()));
    }

    private ProductListing getValidListing() {
        return ProductListing.builder()
                .productUuid(UUID.randomUUID())
                .retailer(defaultRetailer)
                .build();
    }

    // --- Einbindung der Test-Interfaces ---

    @Override
    public JpaRepository<ProductListing, Long> getRepository() {
        return productListingRepository;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Long getEntityId(ProductListing entity) {
        return entity.getId();
    }

    @Override
    public ProductListing buildValidEntity() {
        return getValidListing();
    }

    @Override
    public UUID getUuidOfEntity(ProductListing entity) {
        return entity.getUuid();
    }

    @Override
    public List<ImmutableFieldRule<ProductListing, ?>> getImmutableFieldRules() {
        return List.of(
                new ImmutableFieldRule<>("uuid", ProductListing::getUuid, ProductListing::setUuid, UUID::randomUUID));
    }

    @Override
    public List<NotNullFieldRule<ProductListing, ?>> getNotNullFieldRules() {
        return List.of(
                new NotNullFieldRule<>("uuid"),
                new NotNullFieldRule<>("productUuid"),
                new NotNullFieldRule<>("retailer"));
    }

    @Override
    public List<TrimmedFieldRule<ProductListing>> getTrimmedFieldRules() {
        return List.of(
                new TrimmedFieldRule<>("sku", ProductListing::getSku, ProductListing::setSku));
    }

}
