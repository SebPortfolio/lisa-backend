package de.lisa.backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Transactional
public interface IRepositoryTestDataSpecification<T> {
    JpaRepository<T, Long> getRepository();

    EntityManager getEntityManager();

    Long getEntityId(T entity);

    T buildValidEntity();

    default void flushAndClear() {
        this.getEntityManager().flush();
        this.getEntityManager().clear();
    }
}
