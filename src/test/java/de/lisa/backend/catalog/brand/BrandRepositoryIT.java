package de.lisa.backend.catalog.brand;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
class BrandRepositoryIT implements
        IMinLengthStringSpecification<Brand>,
        IMaxLengthStringSpecification<Brand>,
        INotBlankStringSpecification<Brand>,
        IUniqueStringSpecification<Brand>,
        IUuidGenerationSpecification<Brand>,
        INotNullSpecification<Brand>,
        IImmutableFieldSpecification<Brand> {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should initialize brand with privateLabel as false by default")
    void creation_withDefaults_setsPrivateLabelFalse() {
        Brand brand = getValidBrand().build();

        assertThat(brand.isPrivateLabel()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = { "L'Oréal", "ja!", "M&M's", "Müller", "Häagen-Dazs", "Meßmer" })
    @DisplayName("Should handle special characters in brand name")
    void save_specialCharacters_persistsName(String specialName) {
        Brand expected = getValidBrand().name(specialName).build();

        brandRepository.saveAndFlush(expected);
        entityManager.clear();

        Brand actual = brandRepository.findById(expected.getId()).orElseThrow();
        assertThat(actual.getName()).isEqualTo(specialName);
    }

    private Brand.BrandBuilder getValidBrand() {
        return Brand.builder()
                .name("Brand_" + UUID.randomUUID());
    }

    // --- Einbindung der Test-Interfaces ---

    @Override
    public JpaRepository<Brand, Long> getRepository() {
        return brandRepository;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Long getEntityId(Brand entity) {
        return entity.getId();
    }

    @Override
    public Brand buildValidEntity() {
        return getValidBrand()
                .build();
    }

    @Override
    public UUID getUuidOfEntity(Brand entity) {
        return entity.getUuid();
    }

    @Override
    public List<NotNullFieldRule<Brand, ?>> getNotNullFieldRules() {
        return List.of(new NotNullFieldRule<>("uuid", Brand::setUuid));
    }

    @Override
    public List<ImmutableFieldRule<Brand, ?>> getImmutableFieldRules() {
        return List.of(
                new ImmutableFieldRule<>("uuid", Brand::getUuid, Brand::setUuid, UUID::randomUUID));
    }

    @Override
    public List<TrimmedFieldRule<Brand>> getTrimmedFieldRules() {
        return List.of(
                new TrimmedFieldRule<>("name", Brand::getName, Brand::setName));
    }

    @Override
    public List<UniqueFieldRule<Brand>> getUniqueFieldRules() {
        return List.of(
                new UniqueFieldRule<>("name", Brand::getName, Brand::setName));
    }

    @Override
    public List<NotBlankFieldRule<Brand>> getNotBlankFieldRules() {
        return List.of(
                new NotBlankFieldRule<>("name", Brand::getName, Brand::setName));
    }

    @Override
    public List<MaxLengthFieldRule<Brand>> getMaxLengthFieldRules() {
        return List.of(
                new MaxLengthFieldRule<>("name", 50, Brand::getName, Brand::setName));
    }

    @Override
    public List<MinLengthFieldRule<Brand>> getMinLengthFieldRules() {
        return List.of(
                new MinLengthFieldRule<>("name", 2, Brand::getName, Brand::setName));
    }
}
