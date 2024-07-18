package org.kirill.space.cell.simple_rest_service.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.kirill.space.cell.simple_rest_service.model.Task;
import org.kirill.space.cell.simple_rest_service.repository.InMemTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class TaskRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    InMemTaskRepository repository;

    @AfterEach
    void tearDown() {
        this.repository.getTasks().clear();
    }

    @Test
    void handleGetAllTask_ReturnValidResponseEntity() throws Exception {
        // given
        var requestBuilder = get("/api/tasks");
        this.repository.getTasks()
                .addAll(List.of(new Task(UUID.fromString("ffddc01e-44f9-11ef-95c8-bac3b850da89"),
                                "First Task", false),
                        new Task(UUID.fromString("016a983a-44fa-11ef-94b1-bac3b850da89"),
                                "Second Task", true)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                [
                                    {
                                        "id": "ffddc01e-44f9-11ef-95c8-bac3b850da89",
                                                                                    "details": "First Task",
                                                                                    "completed": false
                                    },
                                    {
                                        "id": "016a983a-44fa-11ef-94b1-bac3b850da89",
                                                                                    "details": "Second Task",
                                                                                    "completed": true
                                    }
                                ]
                                """)
                );
    }

    @Test
    void handleCreateNewTask_PayloadIsValid_ReturnValidResponseEntity() throws Exception {
        // given
        var requestBuilder = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "details": "Third Task"
                        }
                        """);
        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().isCreated(),
                        header().exists(HttpHeaders.LOCATION),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                "details": "Third Task",
                                "completed": false
                                }
                                """),
                        jsonPath("$.id").exists()
                );

        assertEquals(1, this.repository.getTasks().size());
        final var task = this.repository.getTasks().get(0);
        assertNotNull(task.id());
        assertEquals("Third Task", this.repository.getTasks().get(0).details());
        assertFalse(this.repository.getTasks().get(0).completed());
    }

    @Test
    void handleCreateNewTask_PayloadIsInvalid_ReturnValidResponseEntity() throws Exception {
        // given
        var requestBuilder = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en")
                .content("""
                        {
                        "details": null
                        }
                        """);
        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().isBadRequest(),
                        header().doesNotExist(HttpHeaders.LOCATION),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                "error": ["Task details must be set"]
                                }
                                """, true)
                );

        assertTrue(this.repository.getTasks().isEmpty());
    }
}
