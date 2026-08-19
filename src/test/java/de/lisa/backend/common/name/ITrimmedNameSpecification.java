package de.lisa.backend.common.name;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public interface ITrimmedNameSpecification<T> extends INameTestBase<T> {

    @Test
    @DisplayName("Should trim name before saving")
    default void save_untrimmedName_persistsTrimmedValue() {
        String baseName = "TrimMe";
        T expected = buildValidEntityWithName("  " + baseName + "  ");

        expected = getRepository().saveAndFlush(expected);
        flushAndClear();

        T actual = getRepository().findById(getEntityId(expected)).orElseThrow();
        assertThat(getNameOfEntity(actual)).isEqualTo(baseName);
    }

}
