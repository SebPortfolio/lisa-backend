package de.lisa.backend.common.numbers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;
import jakarta.validation.ConstraintViolationException;

public interface IPositiveNumberSpecification<T> extends IRepositoryTestDataSpecification<T>, INumberConverter {

    T buildValidEntity();

    /**
     * Struktur für Felder mit einer @Positive-Validierung.
     * 
     * @param fieldName Name des Parameters in der Entität für evtl. Fehlermeldung
     * @param type      Number-Typ des zu prüfenden Feldes
     * @param setter    Setter, zum injizieren des nicht positiven Werts in die
     *                  Entität
     */
    record PositiveFieldRule<T, N extends Number>(
            String fieldName,
            Class<N> type,
            BiConsumer<T, N> setter) {

        // Bequemlichkeits-Methoden für eine saubere Syntax im Test
        public static <T> PositiveFieldRule<T, Integer> ofInt(String fieldName, BiConsumer<T, Integer> setter) {
            return new PositiveFieldRule<>(fieldName, Integer.class, setter);
        }

        public static <T> PositiveFieldRule<T, Long> ofLong(String fieldName, BiConsumer<T, Long> setter) {
            return new PositiveFieldRule<>(fieldName, Long.class, setter);
        }

        public static <T> PositiveFieldRule<T, BigDecimal> ofBigDecimal(String fieldName,
                BiConsumer<T, BigDecimal> setter) {
            return new PositiveFieldRule<>(fieldName, BigDecimal.class, setter);
        }

        public static <T> PositiveFieldRule<T, Double> ofDouble(String fieldName, BiConsumer<T, Double> setter) {
            return new PositiveFieldRule<>(fieldName, Double.class, setter);
        }
    }

    /**
     * Jede Testklasse gibt hier die Liste ihrer numberischen Felder zurück, die
     * positiv sein müssen.
     */
    List<PositiveFieldRule<T, ?>> getPositiveNumberRules();

    @Test
    @DisplayName("Should throw Exception when positive fields are set to negative values")
    default void save_negativeNumberValues_throwsException() {
        for (PositiveFieldRule<T, ?> rule : getPositiveNumberRules()) {
            T entity = buildValidEntity();
            executeInvalidValueTest(entity, rule, -1, "negative");
        }
    }

    @Test
    @DisplayName("Should throw Exception when positive fields are set to zero")
    default void save_zeroNumberValues_throwsConstraintViolationException() {
        for (PositiveFieldRule<T, ?> rule : getPositiveNumberRules()) {
            T entity = buildValidEntity();
            executeInvalidValueTest(entity, rule, 0, "zero");
        }
    }

    private <N extends Number> void executeInvalidValueTest(
            T entity,
            PositiveFieldRule<T, N> rule,
            int testValue,
            String valueTypeDescription) {

        N invalidValue = convertValue(testValue, rule.type());
        rule.setter().accept(entity, invalidValue);

        assertThrows(
                ConstraintViolationException.class,
                () -> getRepository().saveAndFlush(entity),
                String.format(
                        "Expected field '%s' to reject %s value '%s', but no exception was thrown.",
                        rule.fieldName(), valueTypeDescription, invalidValue));
    }
}
