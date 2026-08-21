package de.lisa.backend.common.datetimeperiod;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;
import jakarta.validation.ConstraintViolationException;

public interface IDateTimePeriodSpecification<T extends DateTimePeriod> extends IRepositoryTestDataSpecification<T> {

    T buildValidEntity();

    @Test
    @DisplayName("Should throw exception when end timestamp is before start timestamp (DB Constraint)")
    default void save_endAtBeforeStartAt_throwsException() {
        T entity = buildValidEntity();
        entity.setStartAt(OffsetDateTime.now());
        entity.setEndAt(OffsetDateTime.now().minusDays(1));

        assertThrows(ConstraintViolationException.class, () -> {
            getRepository().saveAndFlush(entity);
        });
    }

    @Test
    @DisplayName("Should throw exception when start timestamp is null (DB Constraint)")
    default void save_nullStartAt_throwsException() {
        T entity = buildValidEntity();
        entity.setStartAt(null);

        assertThrows(ConstraintViolationException.class, () -> {
            getRepository().saveAndFlush(entity);
        });
    }

    @Test
    @DisplayName("Should not throw exception when end timestamp is null (DB Constraint)")
    default void save_nullEndAt_works() {
        T entity = buildValidEntity();
        entity.setStartAt(OffsetDateTime.now());
        entity.setEndAt(null);

        assertDoesNotThrow(() -> {
            getRepository().saveAndFlush(entity);
        });
    }

    @Test
    @DisplayName("Should allow end timestamp after start timestamp")
    default void save_validInterval_works() {
        T entity = buildValidEntity();
        entity.setStartAt(OffsetDateTime.now());
        entity.setEndAt(OffsetDateTime.now().plusDays(7));

        assertDoesNotThrow(() -> {
            getRepository().saveAndFlush(entity);
        });
    }
}
