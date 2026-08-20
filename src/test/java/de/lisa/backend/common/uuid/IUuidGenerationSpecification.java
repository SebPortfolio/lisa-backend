package de.lisa.backend.common.uuid;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;

public interface IUuidGenerationSpecification<T> extends IRepositoryTestDataSpecification<T> {


    UUID getUuidOfEntity(T entity);

    @Test
    @DisplayName("Should automatically generate UUID and ensure it is NOT NULL on save")
    default void save_newEntity_automaticallyGeneratesNonNullUuid() {
        T expected = buildValidEntity();

        T saved = getRepository().saveAndFlush(expected);
        flushAndClear();

        T actual = getRepository().findById(getEntityId(saved)).orElseThrow();
        assertThat(getUuidOfEntity(actual)).isNotNull();
    }
}
