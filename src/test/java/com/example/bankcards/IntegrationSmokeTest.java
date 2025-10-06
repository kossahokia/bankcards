package com.example.bankcards;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class IntegrationSmokeTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

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
