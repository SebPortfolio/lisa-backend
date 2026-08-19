package de.lisa.backend.common.name;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolationException;

public interface IMaxLengthNameSpecification<T> extends INameTestBase<T> {

    int getMaxNameLength();

    @Test
    @DisplayName("Should throw exception on save when name is too long")
    default void save_tooLongName_throwsException() {
        T entity = buildValidEntityWithName("A".repeat(getMaxNameLength() + 1));
        assertThrows(ConstraintViolationException.class, () -> {
            getRepository().saveAndFlush(entity);
        });
    }
}
