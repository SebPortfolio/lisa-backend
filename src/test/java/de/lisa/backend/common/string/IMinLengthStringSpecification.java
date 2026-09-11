package de.lisa.backend.common.string;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;
import jakarta.validation.ConstraintViolationException;

public interface IMinLengthStringSpecification<T> extends IRepositoryTestDataSpecification<T> {

    /**
     * Regel-Struktur für String-Felder mit Mindestlänge (@Size(max = ...)).
     * 
     * @param fieldName Name des Feldes in der Entität für aussagekräftige
     *                  Fehlermeldungen
     * @param minLength Erforderliche Mindestlänge des Feldes
     * @param getter    Getter, um den Wert aus der Entität auszulesen
     * @param setter    Setter, um den zu kurzen Wert in die Entität einzuspeisen
     */
    record MinLengthFieldRule<T>(
            String fieldName,
            int minLength,
            Function<T, String> getter,
            BiConsumer<T, String> setter) {

        public static <T> MinLengthFieldRule<T> of(
                String fieldName,
                int minLength,
                Function<T, String> getter,
                BiConsumer<T, String> setter) {
            return new MinLengthFieldRule<>(fieldName, minLength, getter, setter);
        }
    }

    /**
     * Liefert alle String-Felder der Entität, die eine Mindestlänge unterschreiten
     * können.
     */
    List<MinLengthFieldRule<T>> getMinLengthFieldRules();

    @Test
    @DisplayName("Should throw exception on save when String field is too short")
    default void save_tooShortStringFields_throwsException() {
        List<MinLengthFieldRule<T>> rules = getMinLengthFieldRules();
        if (rules.isEmpty()) {
            return;
        }

        for (MinLengthFieldRule<T> rule : rules) {
            if (rule.minLength() <= 0) {
                continue;
            }

            T entity = buildValidEntity();
            String tooShortValue = "A".repeat(rule.minLength() - 1);

            rule.setter().accept(entity, tooShortValue);

            assertThatThrownBy(() -> {
                getRepository().saveAndFlush(entity);
                flushAndClear();
            })
                    .as("Expected Exception when field '%s' with min length %d is set to '%s' (length %d)",
                            rule.fieldName(), rule.minLength(), tooShortValue, tooShortValue.length())
                    .isInstanceOf(ConstraintViolationException.class);
        }
    }
}
