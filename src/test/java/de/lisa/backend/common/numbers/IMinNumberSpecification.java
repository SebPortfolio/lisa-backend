package de.lisa.backend.common.numbers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;
import jakarta.validation.ConstraintViolationException;

public interface IMinNumberSpecification<T> extends IRepositoryTestDataSpecification<T>, INumberConverter {
    T buildValidEntity();

    /**
     * Regel-Struktur für Felder mit einem numerischen Minimum via
     * <code>@Min</code>-Validierung.
     * 
     * @param fieldName Name des Parameters in der Entität für aussagekräftige
     *                  Fehlermeldungen
     * @param minValue  Für das Feld definierter <code>@Min</code>-Wert (z. B. 0
     *                  oder 1)
     * @param type      Number-Typ des zu prüfenden Feldes
     * @param setter    Setter, um den ungültigen Wert in die Entität einzuspeisen
     */
    record MinFieldRule<T, N extends Number>(
            String fieldName,
            long minValue,
            Class<N> type,
            BiConsumer<T, N> setter) {

        // Bequemlichkeits-Methoden für eine saubere Syntax im Test
        public static <T> MinFieldRule<T, Integer> ofInt(String fieldName, long minValue,
                BiConsumer<T, Integer> setter) {
            return new MinFieldRule<>(fieldName, minValue, Integer.class, setter);
        }

        public static <T> MinFieldRule<T, Long> ofLong(String fieldName, long minValue, BiConsumer<T, Long> setter) {
            return new MinFieldRule<>(fieldName, minValue, Long.class, setter);
        }

        public static <T> MinFieldRule<T, BigDecimal> ofBigDecimal(String fieldName, long minValue,
                BiConsumer<T, BigDecimal> setter) {
            return new MinFieldRule<>(fieldName, minValue, BigDecimal.class, setter);
        }

        public static <T> MinFieldRule<T, Double> ofDouble(String fieldName, long minValue,
                BiConsumer<T, Double> setter) {
            return new MinFieldRule<>(fieldName, minValue, Double.class, setter);
        }
    }

    /**
     * Jede Testklasse gibt hier die Liste ihrer numerischen Felder zurück, die
     * einen Minimalwert unterschreiten könnten.
     */
    List<MinFieldRule<T, ?>> getMinNumberRules();

    @Test
    @DisplayName("Should throw Exception when fields underflow their configured @Min value")
    default void save_valuesBelowMinimum_throwsException() {
        List<MinFieldRule<T, ?>> rules = getMinNumberRules();

        for (MinFieldRule<T, ?> rule : rules) {
            T entity = buildValidEntity();
            testRule(entity, rule);
        }
    }

    private <N extends Number> void testRule(T entity, MinFieldRule<T, N> rule) {
        N invalidValue = calculateInvalidValue(rule.minValue(), rule.type());

        rule.setter().accept(entity, invalidValue);

        assertThrows(
                ConstraintViolationException.class,
                () -> getRepository().saveAndFlush(entity),
                String.format(
                        "Expected field '%s' to reject value '%s' (configured min: %d), but no exception was thrown.",
                        rule.fieldName(), invalidValue, rule.minValue()));
    }

    private <N extends Number> N calculateInvalidValue(long minValue, Class<N> type) {
        long invalidLong = minValue - 1;

        return convertValue(Long.valueOf(invalidLong).intValue(), type);
    }
}
