package de.lisa.backend.common.string;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;
import jakarta.validation.ConstraintViolationException;

// FIXME: Verschieben in das Modul 'lisa-common-test' unter src/main/java, 
// sobald die Microservice-Aufteilung erfolgt, damit andere Module diese
// Test-Specs über den <scope>test</scope> wiederverwenden können.

public interface INotBlankStringSpecification<T> extends IRepositoryTestDataSpecification<T> {

    /**
     * Regel-Struktur für nicht leere String-Felder mit einer
     * <code>@NotBlank</code>-Validierung.
     * 
     * @param fieldName Name des Parameters in der Entität für aussagekräftige
     *                  Fehlermeldungen
     * @param getter    Getter, um den verarbeiteten Wert aus der Entität auszulesen
     * @param setter    Setter, um den ungültigen Wert in die Entität einzuspeisen
     */
    record NotBlankFieldRule<T>(
            String fieldName,
            Function<T, String> getter,
            BiConsumer<T, String> setter) {

        public static <T> NotBlankFieldRule<T> of(
                String fieldName,
                Function<T, String> getter,
                BiConsumer<T, String> setter) {
            return new NotBlankFieldRule<>(fieldName, getter, setter);
        }
    }

    /**
     * Liefert alle String-Felder der Entität, die nicht null, leer oder blank sein
     * dürfen.
     */
    List<NotBlankFieldRule<T>> getNotBlankFieldRules();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "\t", "\n" })
    @DisplayName("Should throw exception on save with blank String fields")
    default void save_blankStringFields_throwsException(String invalidValue) {
        List<NotBlankFieldRule<T>> rules = getNotBlankFieldRules();
        if (rules.isEmpty()) {
            return;
        }

        for (NotBlankFieldRule<T> rule : rules) {
            T entity = buildValidEntity();

            if (invalidValue == null) {
                ReflectionTestUtils.setField(entity, rule.fieldName(), null);
            } else {
                rule.setter().accept(entity, invalidValue);
            }

            assertThatThrownBy(() -> {
                getRepository().saveAndFlush(entity);
                flushAndClear();
            })
                    .as("Expected exception when field '%s' is set to '%s'",
                            rule.fieldName(),
                            invalidValue == null ? "null" : invalidValue.replace("\t", "\\t").replace("\n", "\\n"))
                    .isInstanceOf(ConstraintViolationException.class);
        }
    }

}
