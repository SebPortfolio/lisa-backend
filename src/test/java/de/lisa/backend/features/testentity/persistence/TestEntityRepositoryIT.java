package de.lisa.backend.features.testentity.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestEntityRepositoryIT {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private TestEntityRepository repository;

    @Test
    @DisplayName("Should persist and retrieve a TestEntity from the database")
    void shouldSaveAndRetrieveEntity() {
        TestEntity entity = new TestEntity();
        entity.setName("Integration-Test-Success");

        TestEntity savedEntity = repository.save(entity);

        assertThat(savedEntity.getId()).isNotNull();

        Optional<TestEntity> foundEntity = repository.findById(savedEntity.getId());
        assertThat(foundEntity).isPresent();
        assertThat(foundEntity.get().getName()).isEqualTo("Integration-Test-Success");
    }
}
