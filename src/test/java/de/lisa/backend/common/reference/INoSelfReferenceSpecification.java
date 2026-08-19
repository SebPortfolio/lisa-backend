package de.lisa.backend.common.reference;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;
import jakarta.validation.ConstraintViolationException;

public interface INoSelfReferenceSpecification<T> extends IRepositoryTestDataSpecification<T> {

    T buildValidEntity();

    void setParent(T entity, T parent);

    @Test
    @DisplayName("Should throw exception when entity references itself as parent before first save (Transient)")
    default void save_selfReferenceTransient_throwsException() {
        T entity = buildValidEntity();

        setParent(entity, entity);

        assertThrows(ConstraintViolationException.class, () -> getRepository().saveAndFlush(entity));
    }

    @Test
    @DisplayName("Should throw exception when entity references itself as parent (Persistent - different JVM objects, same ID)")
    default void save_selfReferencePersistent_throwsException() {
        T instanceA = buildValidEntity();
        getRepository().saveAndFlush(instanceA);
        Long savedId = getEntityId(instanceA);

        flushAndClear();

        T instanceB = getRepository().findById(savedId).orElseThrow();

        assertThat(savedId).isEqualTo(getEntityId(instanceB));

        setParent(instanceA, instanceB);

        assertThrows(ConstraintViolationException.class, () -> getRepository().saveAndFlush(instanceA));
    }
}