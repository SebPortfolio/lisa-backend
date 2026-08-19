package de.lisa.backend.common.numbers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;
import jakarta.validation.ConstraintViolationException;

public interface IMaxNumberSpecification<T> extends IRepositoryTestDataSpecification<T>, INumberConverter {
    T buildValidEntity();

    /**
     * Struktur für Felder mit einer @Max-Validierung.
     * 
     * @param fieldName Name des Parameters in der Entität für aussagekräftige
     *                  Fehlermeldungen
     * @param maxValue  In der Entität definierter @Max-Wert (z.B. 0 oder 1)
     * @param type      Number-Typ des zu prüfenden Feldes
     * @param setter    Setter, um den ungültigen Wert in die Entität einzuspeisen
     */
    record MaxFieldRule<T, N extends Number>(
            String fieldName,
            long maxValue,
            Class<N> type,
            BiConsumer<T, N> setter) {

        // Bequemlichkeits-Methoden für eine saubere Syntax im Test
        public static <T> MaxFieldRule<T, Integer> ofInt(String fieldName, long maxValue,
                BiConsumer<T, Integer> setter) {
            return new MaxFieldRule<>(fieldName, maxValue, Integer.class, setter);
        }

        public static <T> MaxFieldRule<T, Long> ofLong(String fieldName, long maxValue, BiConsumer<T, Long> setter) {
            return new MaxFieldRule<>(fieldName, maxValue, Long.class, setter);
        }

        public static <T> MaxFieldRule<T, BigDecimal> ofBigDecimal(String fieldName, long maxValue,
                BiConsumer<T, BigDecimal> setter) {
            return new MaxFieldRule<>(fieldName, maxValue, BigDecimal.class, setter);
        }

        public static <T> MaxFieldRule<T, Double> ofDouble(String fieldName, long maxValue,
                BiConsumer<T, Double> setter) {
            return new MaxFieldRule<>(fieldName, maxValue, Double.class, setter);
        }
    }

    /**
     * Jede Testklasse gibt hier die Liste ihrer numerischen Felder zurück, die
     * einen Maxdestwert unterschreiten könnten.
     */
    List<MaxFieldRule<T, ?>> getMaxNumberRules();

    @Test
    @DisplayName("Should throw Exception when fields overflow their configured @Max value")
    default void save_valuesAboveMaximum_throwsException() {
        List<MaxFieldRule<T, ?>> rules = getMaxNumberRules();

        for (MaxFieldRule<T, ?> rule : rules) {
            T entity = buildValidEntity();
            testRule(entity, rule);
        }
    }

    private <N extends Number> void testRule(T entity, MaxFieldRule<T, N> rule) {
        N invalidValue = calculateInvalidValue(rule.maxValue(), rule.type());

        rule.setter().accept(entity, invalidValue);

        assertThrows(
                ConstraintViolationException.class,
                () -> getRepository().saveAndFlush(entity),
                String.format(
                        "Expected field '%s' to reject value '%s' (configured max: %d), but no exception was thrown.",
                        rule.fieldName(), invalidValue, rule.maxValue()));
    }

    private <N extends Number> N calculateInvalidValue(long maxValue, Class<N> type) {
        long invalidLong = maxValue + 1;

        return convertValue(Long.valueOf(invalidLong).intValue(), type);
    }

}
