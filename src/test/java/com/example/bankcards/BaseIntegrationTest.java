package com.example.bankcards;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 🔧 Base class for all full-context integration tests.
 *
 * <p>
 * Provides a preconfigured Spring Boot test environment with
 * {@link MockMvc} and {@link ObjectMapper} autowired for convenient
 * end-to-end and controller-level integration testing.
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>🎯 Launches the complete Spring Boot context via {@link SpringBootTest}.</li>
 *   <li>🧪 Enables {@link MockMvc} for HTTP endpoint testing without deploying a server.</li>
 *   <li>🧩 Activates <code>test</code> profile to load in-memory or mock infrastructure.</li>
 *   <li>🔄 Wraps each test in a transaction that rolls back automatically after completion.</li>
 *   <li>📦 Provides {@link ObjectMapper} for JSON serialization/deserialization of DTOs.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * class AuthControllerIT extends BaseIntegrationTest {
 *     @Test
 *     void login_ReturnsJwtToken() throws Exception {
 *         mockMvc.perform(post("/api/auth/login")
 *                 .contentType(MediaType.APPLICATION_JSON)
 *                 .content(objectMapper.writeValueAsString(loginRequest)))
 *                 .andExpect(status().isOk());
 *     }
 * }
 * }</pre>
 *
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
 * @see org.springframework.transaction.annotation.Transactional
 * @see org.springframework.test.web.servlet.MockMvc
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    /** Provides a MockMvc instance for performing HTTP requests against controllers. */
    @Autowired protected MockMvc mockMvc;

    /** Jackson ObjectMapper for serializing/deserializing JSON bodies in integration tests. */
    @Autowired protected ObjectMapper objectMapper;
}
