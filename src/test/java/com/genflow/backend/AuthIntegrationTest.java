package com.genflow.backend;

import com.genflow.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

  

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerAndLogin_shouldWorkEndToEnd()
            throws Exception {

        // 1. Register a real user
        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test User",
                                  "email": "integration@test.com",
                                  "password": "Password123"
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.email")
                        .value("integration@test.com")
        )
        .andExpect(
                jsonPath("$.name")
                        .value("Test User")
        );

        // 2. Verify user was really stored in H2
        boolean userExists =
                userRepository.existsByEmail(
                        "integration@test.com"
                );

        if (!userExists) {
            throw new AssertionError(
                    "Registered user was not stored in database"
            );
        }

        // 3. Login using the registered credentials
        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "integration@test.com",
                                  "password": "Password123"
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.token").isNotEmpty()
        )
        .andExpect(
                jsonPath("$.user.email")
                        .value("integration@test.com")
        );
    }
}