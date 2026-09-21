package com.genflow.backend.controller;

import com.genflow.backend.dto.GenerationResponse;
import com.genflow.backend.entity.GenerationType;
import com.genflow.backend.service.GenerationService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.genflow.backend.service.JwtService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.genflow.backend.repository.UserRepository;

import org.springframework.cache.CacheManager;

@WebMvcTest(GenerationController.class)
class GenerationControllerTest {

   @Autowired
private MockMvc mockMvc;

@MockitoBean
private GenerationService generationService;

@MockitoBean
private JwtService jwtService;

@MockitoBean
private UserRepository userRepository;

@MockitoBean
private CacheManager cacheManager;

    

    @Test
void createGeneration_shouldReturn200AndGeneration()
        throws Exception {

    GenerationResponse response =
            new GenerationResponse(
                    1L,
                    GenerationType.GENERATE,
                    "Explain REST APIs",
                    "REST APIs allow applications to communicate.",
                    "COMPLETED",
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

    when(
            generationService.createGeneration(any())
    ).thenReturn(response);

    mockMvc.perform(
            post("/api/generations")
                    .with(csrf())
                    .with(user("test@example.com").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "type": "GENERATE",
                              "input": "Explain REST APIs"
                            }
                            """)
    )
    .andExpect(status().isOk())
    .andExpect(
            jsonPath("$.id").value(1)
    )
    .andExpect(
            jsonPath("$.type").value("GENERATE")
    )
    .andExpect(
            jsonPath("$.input").value("Explain REST APIs")
    )
    .andExpect(
            jsonPath("$.status").value("COMPLETED")
    );
}

@Test
void createGeneration_shouldReturn400ForInvalidRequest()
        throws Exception {

    mockMvc.perform(
            post("/api/generations")
                    .with(csrf())
                    .with(user("test@example.com").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "type": "GENERATE",
                              "input": ""
                            }
                            """)
    )
    .andExpect(status().isBadRequest());
}


@Test
void createGeneration_shouldReturn401WhenNotAuthenticated()
        throws Exception {

    mockMvc.perform(
            post("/api/generations")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "type": "GENERATE",
                              "input": "Explain REST APIs"
                            }
                            """)
    )
    .andExpect(status().isUnauthorized());
}
}