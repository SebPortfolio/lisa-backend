package de.lisa.backend.common.name;

import de.lisa.backend.common.repository.IRepositoryTestDataSpecification;

public interface INameTestBase<T> extends IRepositoryTestDataSpecification<T> {

    T buildValidEntityWithName(String name);

    String getNameOfEntity(T entity);

}
