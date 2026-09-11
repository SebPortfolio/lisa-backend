package de.lisa.backend.common.string;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Test-Interface zur einheitlichen Prüfung eines einzigartigen Strings.
 * Bedingt, dass der String bereits getrimmt worden ist.
 */
public interface IUniqueStringSpecification<T> extends ITrimmedStringSpecification<T> {

    /**
     * Regel-Struktur für eindeutige (unique) String-Felder.
     * 
     * @param fieldName Name des Feldes in der Entität für aussagekräftige
     *                  Fehlermeldungen
     * @param getter    Getter, um den Wert aus der Entität auszulesen
     * @param setter    Setter, um den Wert in die Entität einzuspeisen
     */
    record UniqueFieldRule<T>(
            String fieldName,
            Function<T, String> getter,
            BiConsumer<T, String> setter) {

        public static <T> UniqueFieldRule<T> of(
                String fieldName,
                Function<T, String> getter,
                BiConsumer<T, String> setter) {
            return new UniqueFieldRule<>(fieldName, getter, setter);
        }
    }

    /**
     * Liefert alle String-Felder der Entität, die in der Datenbank eindeutig sein
     * müssen.
     */
    List<UniqueFieldRule<T>> getUniqueFieldRules();

    @Test
    @DisplayName("Should throw Exception on save with duplicate field values")
    default void save_nonUniqueField_throwsException() {
        List<UniqueFieldRule<T>> rules = getUniqueFieldRules();
        if (rules.isEmpty()) {
            return;
        }

        for (int i = 0; i < rules.size(); i++) {
            UniqueFieldRule<T> rule = rules.get(i);
            String duplicateValue = "UniqueVal_" + i;

            T entity1 = buildValidEntity();
            rule.setter().accept(entity1, duplicateValue);
            getRepository().saveAndFlush(entity1);
            flushAndClear();

            T entity2 = buildValidEntity();
            rule.setter().accept(entity2, duplicateValue);

            assertThatThrownBy(() -> {
                getRepository().saveAndFlush(entity2);
                flushAndClear();
            })
                    .as("Expected Exception when duplicate value '%s' is set for unique field '%s'",
                            duplicateValue, rule.fieldName())
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Test
    @DisplayName("Should throw Exception on save with duplicate field values after trimming")
    default void save_nonUniqueFieldUntrimmed_throwsException() {
        List<UniqueFieldRule<T>> rules = getUniqueFieldRules();
        if (rules.isEmpty()) {
            return;
        }

        for (int i = 0; i < rules.size(); i++) {
            UniqueFieldRule<T> rule = rules.get(i);
            String baseValue = "TrimmedUniqueVal_" + i;

            T entity1 = buildValidEntity();
            rule.setter().accept(entity1, baseValue);
            getRepository().saveAndFlush(entity1);
            flushAndClear();

            T entity2 = buildValidEntity();
            rule.setter().accept(entity2, "  " + baseValue + "  ");

            assertThatThrownBy(() -> {
                getRepository().saveAndFlush(entity2);
                flushAndClear();
            })
                    .as("Expected Exception when untrimmed duplicate value '  %s  ' is set for unique field '%s'",
                            baseValue, rule.fieldName())
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}