package de.lisa.backend.common.field;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;

public interface IImmutableFieldSpecification<T> extends IRepositoryTestDataSpecification<T> {

    /**
     * Struktur für unveränderliche Felder.
     * 
     * @param getter           Getter für den aktuellen Wert
     * @param setter           Setter, um einen neuen Testwert zu setzen
     * @param newValueSupplier Supplier, der einen neuen, abweichenden Wert für den
     *                         Update-Versuch liefert
     */
    record ImmutableFieldRule<T, V>(
            Function<T, V> getter,
            BiConsumer<T, V> setter,
            Supplier<V> newValueSupplier) {
    }

    /**
     * Jede Testklasse gibt hier die Liste ihrer unveraenderlichen Felder zurück.
     */
    List<ImmutableFieldRule<T, ?>> getImmutableFieldRules();

    @Test
    @DisplayName("Should NOT update fields marked as updatable=false even if changed in Java object")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    default void update_manuallyChangedImmutableFields_remainsUnchangedInDatabase() {
        T entity = buildValidEntity();
        T saved = getRepository().saveAndFlush(entity);
        flushAndClear();

        T fetchedForOriginal = getRepository().findById(getEntityId(saved)).orElseThrow();

        // Für alle definierten Regeln den Originalwert wegsichern und den Wert
        // manipulieren
        List<ImmutableFieldRule<T, ?>> rules = getImmutableFieldRules();
        Object[] originalValues = new Object[rules.size()];

        for (int i = 0; i < rules.size(); i++) {
            ImmutableFieldRule rule = rules.get(i);
            originalValues[i] = rule.getter().apply(fetchedForOriginal);

            // Setze den manipulierten Wert auf dem persistenten Objekt
            rule.setter().accept(fetchedForOriginal, rule.newValueSupplier().get());
        }

        getRepository().saveAndFlush(fetchedForOriginal);
        flushAndClear();

        T finalFetched = getRepository().findById(getEntityId(saved)).orElseThrow();
        for (int i = 0; i < rules.size(); i++) {
            ImmutableFieldRule rule = rules.get(i);
            Object dbValue = rule.getter().apply(finalFetched);
            assertThat(dbValue).isEqualTo(originalValues[i]);
        }
    }
}
