package de.lisa.backend.common.string;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;

public interface ITrimmedStringSpecification<T> extends IRepositoryTestDataSpecification<T> {

    /**
     * Regel-Struktur für zu trimmende String-Felder.
     * 
     * @param fieldName Name des Parameters in der Entität für aussagekräftige
     *                  Fehlermeldungen
     * @param getter    Getter, um den verarbeiteten Wert aus der Entität auszulesen
     * @param setter    Setter, um den ungültigen Wert in die Entität einzuspeisen
     */
    record TrimmedFieldRule<T>(
            String fieldName,
            Function<T, String> getter,
            BiConsumer<T, String> setter) {

        public static <T> TrimmedFieldRule<T> of(
                String fieldName,
                Function<T, String> getter,
                BiConsumer<T, String> setter) {
            return new TrimmedFieldRule<>(fieldName, getter, setter);
        }
    }

    /**
     * Liefert alle String-Felder der Entität, die beim Speichern automatisch
     * getrimmt werden müssen.
     */
    List<TrimmedFieldRule<T>> getTrimmedFieldRules();

    @Test
    @DisplayName("Should trim untrimmed String fields before saving")
    default void save_untrimmedStringFields_persistsTrimmedValues() {
        List<TrimmedFieldRule<T>> rules = getTrimmedFieldRules();
        if (rules.isEmpty()) {
            return;
        }

        T entity = buildValidEntity();
        String[] expectedValues = new String[rules.size()];

        // Felder mit ungetrimmten Werten befüllen & Erwartungswerte merken
        for (int i = 0; i < rules.size(); i++) {
            TrimmedFieldRule<T> rule = rules.get(i);
            String rawValue = "  TrimMe_" + i + "  ";

            rule.setter().accept(entity, rawValue);
            expectedValues[i] = rawValue.trim(); // Erwartungswert ist "TrimMe_i"
        }

        T saved = getRepository().saveAndFlush(entity);
        flushAndClear();

        T fetched = getRepository().findById(getEntityId(saved)).orElseThrow();

        // Prüfen, ob alle Werte getrimmt in der DB gelandet sind
        for (int i = 0; i < rules.size(); i++) {
            TrimmedFieldRule<T> rule = rules.get(i);
            String actualValue = rule.getter().apply(fetched);
            String expectedValue = expectedValues[i];

            assertThat(actualValue)
                    .as("Expected field '%s' to be trimmed to '%s' but was '%s'",
                            rule.fieldName, expectedValue, actualValue)
                    .isEqualTo(expectedValue);
        }
    }

}
