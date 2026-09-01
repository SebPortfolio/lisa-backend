package de.lisa.backend.availability.retailerbrandlisting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.lisa.backend.availability.retailer.Retailer;
import de.lisa.backend.availability.retailer.RetailerRepository;
import de.lisa.backend.common.datetimeperiod.IDateTimePeriodSpecification;
import de.lisa.backend.common.field.INotNullSpecification;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class RetailerBrandListingRepositoryIT implements
        IDateTimePeriodSpecification<RetailerBrandListing>,
        INotNullSpecification<RetailerBrandListing> {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RetailerBrandListingRepository retailerBrandListingRepository;

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

    @Test
    @DisplayName("Should successfully persist and reload a valid retailer brand listing")
    void save_validListing_persistsAndCanBeRetrieved() {
        RetailerBrandListing validListing = getValidListing()
                .build();

        retailerBrandListingRepository.save(validListing);
        Long expectedId = validListing.getId();

        entityManager.clear();

        RetailerBrandListing actual = retailerBrandListingRepository.findById(expectedId).orElseGet(null);
        assertThat(actual.getId()).isEqualTo(expectedId);
        assertThat(actual.getBrandUuid()).isEqualTo(validListing.getBrandUuid());
        assertThat(actual.getRetailer().getId()).isEqualTo(defaultRetailer.getId());
    }

    @Test
    @DisplayName("Should throw exception when saving two listings with same brandUuid and retailer and end timestamp is null (unique index)")
    void save_doubleListingWithNullEnd_throwsException() {
        RetailerBrandListing listing1 = getValidListing()
                .build();

        retailerBrandListingRepository.saveAndFlush(listing1);

        RetailerBrandListing listing2 = getValidListing()
                .brandUuid(listing1.getBrandUuid())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            retailerBrandListingRepository.saveAndFlush(listing2);
        });
    }

    @Test
    @DisplayName("Should not throw exception when saving two listings with same brandUuid and retailer but one end timestamp is non null (unique index)")
    void save_doubleListingWithOneNonNullEnd_persists() {
        RetailerBrandListing listing1 = getValidListing()
                .endAt(OffsetDateTime.now().plusDays(7))
                .build();

        retailerBrandListingRepository.saveAndFlush(listing1);

        RetailerBrandListing listing2 = getValidListing()
                .brandUuid(listing1.getBrandUuid())
                .endAt(null)
                .build();

        assertDoesNotThrow(() -> getRepository().saveAndFlush(listing2));
    }

    private RetailerBrandListing.RetailerBrandListingBuilder<?, ?> getValidListing() {
        return RetailerBrandListing.builder()
                .brandUuid(UUID.randomUUID())
                .retailer(defaultRetailer)
                .startAt(OffsetDateTime.now());
    }

    // --- Einbindung der Test-Interfaces ---

    @Override
    public JpaRepository<RetailerBrandListing, Long> getRepository() {
        return retailerBrandListingRepository;
    }

    @Override
    public RetailerBrandListing buildValidEntity() {
        return getValidListing()
                .build();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Long getEntityId(RetailerBrandListing entity) {
        return entity.getId();
    }

    @Override
    public List<NotNullFieldRule<RetailerBrandListing, ?>> getNotNullFieldRules() {
        return List.of(
                new NotNullFieldRule<>("retailer", RetailerBrandListing::setRetailer),
                new NotNullFieldRule<>("brandUuid", RetailerBrandListing::setBrandUuid));
    }
}
