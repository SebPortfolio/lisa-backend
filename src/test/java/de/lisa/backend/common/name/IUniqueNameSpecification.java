package de.lisa.backend.common.name;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Test-Interface zur einheitlichen Prüfung eines einzigartigen Names.
 * Bedingt, dass der Name bereits getrimmt worden ist.
 */
public interface IUniqueNameSpecification<T> extends ITrimmedNameSpecification<T> {

    @Test
    @DisplayName("Should throw exception on save with non unique name")
    default void save_nonUniqueName_throwsException() {
        T entity1 = buildValidEntityWithName("UniqueName");
        getRepository().saveAndFlush(entity1);

        T entity2 = buildValidEntityWithName("UniqueName");
        assertThrows(DataIntegrityViolationException.class, () -> {
            getRepository().saveAndFlush(entity2);
        });
    }

    @Test
    @DisplayName("Should throw exception on save with non unique untrimmed name")
    default void save_nonUniqueNameAfterTrimming_throwsException() {
        T entity1 = buildValidEntityWithName("UniqueName");
        getRepository().saveAndFlush(entity1);

        T entity2 = buildValidEntityWithName("  UniqueName  ");
        assertThrows(DataIntegrityViolationException.class, () -> {
            getRepository().saveAndFlush(entity2);
        });
    }
}