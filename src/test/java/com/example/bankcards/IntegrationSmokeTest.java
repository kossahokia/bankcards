package com.example.bankcards;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 🔥 Integration smoke test for the entire BankCards application context.
 *
 * <p>
 * This test serves as a lightweight <strong>sanity check</strong> that verifies
 * the Spring Boot application starts correctly and that the persistence layer
 * (JPA repositories, entity mappings, and database connection) works as expected
 * under the <code>test</code> profile.
 * </p>
 *
 * <h3>Purpose:</h3>
 * <ul>
 *   <li>Ensure that the Spring context loads without configuration or dependency errors.</li>
 *   <li>Confirm that {@link UserRepository} is properly wired and can perform CRUD operations.</li>
 *   <li>Validate that the in-memory test database is available and functional.</li>
 * </ul>
 *
 * <h3>Testing strategy:</h3>
 * <ul>
 *   <li>Full Spring Boot context is started via {@link SpringBootTest}.</li>
 *   <li>Active profile <code>test</code> is used (configured in <code>application-test.yml</code>).</li>
 *   <li>Assertions performed using AssertJ for fluent readability.</li>
 *   <li>Extends {@link BaseIntegrationTest} for common setup and transactional utilities.</li>
 * </ul>
 *
 * @see com.example.bankcards.BaseIntegrationTest
 * @see com.example.bankcards.repository.UserRepository
 * @see com.example.bankcards.entity.User
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
class IntegrationSmokeTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    /**
     * Verifies that:
     * <ul>
     *   <li>The Spring application context loads successfully.</li>
     *   <li>Database operations through {@link UserRepository} work correctly.</li>
     * </ul>
     *
     * <p>This test inserts a {@link User} entity and validates that it can be retrieved
     * with the same data, confirming that both the persistence context and
     * transaction boundaries are correctly configured.</p>
     */
    @Test
    @DisplayName("✅ Application context loads and database is accessible")
    void contextLoadsAndDatabaseWorks() {
        // Arrange
        User user = User.builder()
                .username("integration_test_user")
                .password("pwd")
                .fullName("Integration Test User")
                .enabled(true)
                .build();

        // Act
        User saved = userRepository.save(user);

        // Assert
        assertThat(saved.getId()).isNotNull();

        assertThat(userRepository.findByUsername("integration_test_user"))
                .isPresent()
                .get()
                .extracting(User::getFullName)
                .isEqualTo("Integration Test User");
    }
}
