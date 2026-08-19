package de.lisa.backend.catalog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.lisa.backend.common.field.INotNullSpecification;
import de.lisa.backend.common.numbers.IMaxNumberSpecification;
import de.lisa.backend.common.numbers.IPositiveNumberSpecification;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class UnitRepositoryIT implements
        IPositiveNumberSpecification<Unit>,
        IMaxNumberSpecification<Unit>,
        INotNullSpecification<Unit> {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private EntityManager entityManager;

    private static final BaseUnitType DEFAULT_TYPE = BaseUnitType.PIECE;
    private static final Integer DEFAULT_FACTOR = 1;

    @Test
    @DisplayName("Should save and fetch a valid Unit")
    void save_validUnit_persistsAndFetchable() {
        Unit unit = getValidUnit().build();

        unitRepository.saveAndFlush(unit);
        entityManager.clear();

        Unit found = unitRepository.findById(unit.getId()).orElseThrow();
        assertThat(found.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(found.getComparisonFactor()).isEqualTo(DEFAULT_FACTOR);
        assertThat(found.isWeightBased()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 1000, 9999 })
    @DisplayName("Should allow saving for valid comparison factors")
    void save_validFactors_persist(Integer validFactor) {
        Unit unit = getValidUnit()
                .comparisonFactor(validFactor)
                .build();

        unitRepository.saveAndFlush(unit);
        entityManager.clear();

        Integer expected = validFactor;

        Unit found = unitRepository.findById(unit.getId()).orElseThrow();
        assertThat(found.getComparisonFactor()).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should throw exception when saving multiple entities with same base unit type and comparision factor (unique index)")
    void save_multipleUnitsWithSameBaseUnitTypeAndComparisonFactor_throwsException() {
        Unit unit1 = getValidUnit().build();

        unitRepository.saveAndFlush(unit1);
        entityManager.clear();

        Unit unit2 = getValidUnit().build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            unitRepository.saveAndFlush(unit2);
        });
    }

    private Unit.UnitBuilder getValidUnit() {
        return Unit.builder()
                .type(DEFAULT_TYPE)
                .comparisonFactor(DEFAULT_FACTOR);
    }

    // --- Einbindung der Test-Interfaces ---

    @Override
    public JpaRepository<Unit, Long> getRepository() {
        return unitRepository;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Long getEntityId(Unit entity) {
        return entity.getId();
    }

    @Override
    public Unit buildValidEntity() {
        return getValidUnit().build();
    }

    @Override
    public List<PositiveFieldRule<Unit, ?>> getPositiveNumberRules() {
        return List.of(
                PositiveFieldRule.ofInt("comparisonFactor", Unit::setComparisonFactor));
    }

    @Override
    public List<NotNullFieldRule<Unit>> getNotNullFieldRules() {
        return List.of(
                new NotNullFieldRule<>("type", (unit, value) -> unit.setType((BaseUnitType) value)),
                new NotNullFieldRule<>("comparisonFactor",
                        (unit, value) -> unit.setComparisonFactor((Integer) value)));
    }

    @Override
    public List<MaxFieldRule<Unit, ?>> getMaxNumberRules() {
        return List.of(
                MaxFieldRule.ofInt("comparisonFactor", 9999, Unit::setComparisonFactor));
    }
}
