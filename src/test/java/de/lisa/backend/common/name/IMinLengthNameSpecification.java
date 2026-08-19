package de.lisa.backend.common.name;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolationException;

public interface IMinLengthNameSpecification<T> extends INameTestBase<T> {

    int getMinNameLength();

    @Test
    @DisplayName("Should throw exception on save when name is too short")
    default void save_tooShortName_throwsException() {
        T entity = buildValidEntityWithName("A".repeat(getMinNameLength() - 1));
        assertThrows(ConstraintViolationException.class, () -> {
            getRepository().saveAndFlush(entity);
        });
    }

}
