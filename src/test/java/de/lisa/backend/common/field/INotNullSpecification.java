package de.lisa.backend.common.field;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;
import jakarta.validation.ConstraintViolationException;

public interface INotNullSpecification<T> extends IRepositoryTestDataSpecification<T> {

    /**
     * Regel-Struktur für Felder mit einer <code>@NotNull</code>-Validierung.
     * 
     * @param fieldName Name des Parameters in der Entität für aussagekräftige
     *                  Fehlermeldungen
     */
    record NotNullFieldRule<T, V>(
            String fieldName) {
    }

    /**
     * Jede Testklasse gibt hier die Liste ihrer Felder zurück, die nicht null sein
     * dürfen.
     */
    List<NotNullFieldRule<T, ?>> getNotNullFieldRules();

    @Test
    @DisplayName("Should throw Exception when required fields are set to null")
    default void save_nullValuesOnRequiredFields_throwsException() {
        List<NotNullFieldRule<T, ?>> rules = getNotNullFieldRules();

        for (NotNullFieldRule<T, ?> rule : rules) {
            T entity = buildValidEntity();

            // ungültigen Zustand (null) injizieren und Lombok @NonNull umgehen
            ReflectionTestUtils.setField(entity, rule.fieldName(), null);

            assertThrows(
                    ConstraintViolationException.class,
                    () -> getRepository().saveAndFlush(entity),
                    String.format("Expected field '%s' to reject null value, but no exception was thrown.",
                            rule.fieldName()));
        }
    }
}