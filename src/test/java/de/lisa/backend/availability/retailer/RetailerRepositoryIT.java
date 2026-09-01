package de.lisa.backend.availability.retailer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.lisa.backend.common.field.IImmutableFieldSpecification;
import de.lisa.backend.common.field.INotNullSpecification;
import de.lisa.backend.common.string.IMaxLengthStringSpecification;
import de.lisa.backend.common.string.IMinLengthStringSpecification;
import de.lisa.backend.common.string.INotBlankStringSpecification;
import de.lisa.backend.common.string.IUniqueStringSpecification;
import de.lisa.backend.common.uuid.IUuidGenerationSpecification;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class RetailerRepositoryIT implements
        IMinLengthStringSpecification<Retailer>,
        IMaxLengthStringSpecification<Retailer>,
        INotBlankStringSpecification<Retailer>,
        IUniqueStringSpecification<Retailer>,
        IUuidGenerationSpecification<Retailer>,
        IImmutableFieldSpecification<Retailer>,
        INotNullSpecification<Retailer> {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RetailerRepository retailerRepository;

    @ParameterizedTest
    @ValueSource(strings = { "Aldi Süd", "nah & frisch", "V-Markt" })
    @DisplayName("Should handle special characters in retailer name")
    void save_specialCharacters_persistsName(String specialName) {
        Retailer retailer = getValidRetailer().name(specialName).build();

        Retailer saved = retailerRepository.saveAndFlush(retailer);
        entityManager.clear();

        Retailer fetched = retailerRepository.findById(saved.getId()).orElseThrow();
        assertThat(fetched.getName()).isEqualTo(specialName);
    }

    @Test
    @DisplayName("Should successfully find an existing retailer by its name")
    void findByName_withExistingName_returnsRetailer() {
        Retailer expected = getValidRetailer().build();

        retailerRepository.saveAndFlush(expected);

        entityManager.clear();

        Optional<Retailer> actual = retailerRepository.findByName(expected.getName());

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(expected.getId());
        assertThat(actual.get().getUuid()).isEqualTo(expected.getUuid());
        assertThat(actual.get().getName()).isEqualTo(expected.getName());
    }

    @Test
    @DisplayName("Should return empty optional when retailer name does not exist")
    void findByName_whenRetailerDoesNotExist_returnsEmptyOptional() {
        Optional<Retailer> actual = retailerRepository.findByName("NonExistingName");
        assertThat(actual).isEmpty();
    }

    private Retailer.RetailerBuilder getValidRetailer() {
        return Retailer.builder()
                .name("Retailer_" + UUID.randomUUID());
    }

    // --- Einbindung der Test-Interfaces ---

    @Override
    public JpaRepository<Retailer, Long> getRepository() {
        return retailerRepository;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Retailer buildValidEntity() {
        return getValidRetailer()
                .name("Retailer_" + UUID.randomUUID())
                .build();
    }

    @Override
    public Long getEntityId(Retailer entity) {
        return entity.getId();
    }

    @Override
    public UUID getUuidOfEntity(Retailer entity) {
        return entity.getUuid();
    }

    @Override
    public List<ImmutableFieldRule<Retailer, ?>> getImmutableFieldRules() {
        return List.of(
                new ImmutableFieldRule<>("uuid", Retailer::getUuid, Retailer::setUuid, UUID::randomUUID));
    }

    @Override
    public List<NotNullFieldRule<Retailer, ?>> getNotNullFieldRules() {
        return List.of(
                new NotNullFieldRule<>("uuid", Retailer::setUuid));
    }

    @Override
    public List<TrimmedFieldRule<Retailer>> getTrimmedFieldRules() {
        return List.of(
                new TrimmedFieldRule<>("name", Retailer::getName, Retailer::setName));
    }

    @Override
    public List<UniqueFieldRule<Retailer>> getUniqueFieldRules() {
        return List.of(
                new UniqueFieldRule<>("name", Retailer::getName, Retailer::setName));
    }

    @Override
    public List<NotBlankFieldRule<Retailer>> getNotBlankFieldRules() {
        return List.of(
                new NotBlankFieldRule<>("name", Retailer::getName, Retailer::setName));
    }

    @Override
    public List<MaxLengthFieldRule<Retailer>> getMaxLengthFieldRules() {
        return List.of(
                new MaxLengthFieldRule<>("name", 50, Retailer::getName, Retailer::setName));
    }

    @Override
    public List<MinLengthFieldRule<Retailer>> getMinLengthFieldRules() {
        return List.of(
                new MinLengthFieldRule<>("name", 2, Retailer::getName, Retailer::setName));
    }
}
