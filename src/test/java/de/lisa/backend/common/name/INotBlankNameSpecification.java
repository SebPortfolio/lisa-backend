package de.lisa.backend.common.name;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolationException;

// FIXME: Verschieben in das Modul 'lisa-common-test' unter src/main/java, 
// sobald die Microservice-Aufteilung erfolgt, damit andere Module diese
// Test-Specs über den <scope>test</scope> wiederverwenden können.

public interface INotBlankNameSpecification<T> extends INameTestBase<T> {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "  ", "\t", "\n" })
    @DisplayName("Should throw exception on save with blank name")
    default void save_blankName_throwsException(String name) {
        T entity = buildValidEntityWithName(name);
        assertThrows(ConstraintViolationException.class, () -> {
            getRepository().saveAndFlush(entity);
        });
    }

}
