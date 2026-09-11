package de.lisa.backend.common.string;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;
import jakarta.validation.ConstraintViolationException;

public interface IMaxLengthStringSpecification<T> extends IRepositoryTestDataSpecification<T> {

    /**
     * Regel-Struktur für String-Felder mit Maximallänge
     * <code>@Size(max = ...)</code>.
     * 
     * @param fieldName Name des Feldes in der Entität für aussagekräftige
     *                  Fehlermeldungen
     * @param maxLength Für das Feld definierter <code>@Size(max = ...)</code>-Wert
     *                  (z. B. 0 oder 1)
     * @param getter    Getter, um den Wert aus der Entität auszulesen
     * @param setter    Setter, um den zu kurzen Wert in die Entität einzuspeisen
     */
    record MaxLengthFieldRule<T>(
            String fieldName,
            int maxLength,
            Function<T, String> getter,
            BiConsumer<T, String> setter) {

        public static <T> MaxLengthFieldRule<T> of(
                String fieldName,
                int maxLength,
                Function<T, String> getter,
                BiConsumer<T, String> setter) {
            return new MaxLengthFieldRule<>(fieldName, maxLength, getter, setter);
        }
    }

    /**
     * Liefert alle String-Felder der Entität, die eine Maximallänge überschreiten
     * können.
     */
    List<MaxLengthFieldRule<T>> getMaxLengthFieldRules();

    @Test
    @DisplayName("Should throw exception on save when String field is too long")
    default void save_tooLongStringFields_throwsException() {
        List<MaxLengthFieldRule<T>> rules = getMaxLengthFieldRules();
        if (rules.isEmpty()) {
            return;
        }

        for (MaxLengthFieldRule<T> rule : rules) {
            if (rule.maxLength() <= 0) {
                continue;
            }

            T entity = buildValidEntity();
            String tooLongValue = "A".repeat(rule.maxLength() + 1);

            rule.setter().accept(entity, tooLongValue);

            assertThatThrownBy(() -> {
                getRepository().saveAndFlush(entity);
                flushAndClear();
            })
                    .as("Expected Exception when field '%s' with max length %d is set to '%s' (length %d)",
                            rule.fieldName(), rule.maxLength(), tooLongValue, tooLongValue.length())
                    .isInstanceOf(ConstraintViolationException.class);
        }
    }
}
