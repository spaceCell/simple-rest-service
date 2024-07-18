package org.kirill.space.cell.simple_rest_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kirill.space.cell.simple_rest_service.model.ErrorPresentation;
import org.kirill.space.cell.simple_rest_service.model.NewTaskPayload;
import org.kirill.space.cell.simple_rest_service.model.Task;
import org.kirill.space.cell.simple_rest_service.repository.TaskRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRestControllerTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    TaskRestController controller;

    @Test
    @DisplayName("GET /api/tasks return HTTP-answer with status 200 and list task")
    void handleGetAllTask_ReturnValidResponseEntity() {
        // given
        var tasks = List.of(new Task(UUID.randomUUID(), "First Task", false),
                new Task(UUID.randomUUID(), "Second Task", true));
        doReturn(tasks).when(this.taskRepository).findAll();

        // when
        var responseEntity = this.controller.handleGetAllTask();

        // then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertEquals(tasks, responseEntity.getBody());
    }

    @Test
    void handleCreateNewTask_PayloadIsValid_ReturnValidResponseEntity() {
        // given
        var details = "Third Task";

        // when
        var responseEntity = this.controller.handleCreateNewTask(new NewTaskPayload(details),
                UriComponentsBuilder.fromUriString("http://localhost:8080"), Locale.ENGLISH);

        // then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        if (responseEntity.getBody() instanceof Task task) {
            assertNotNull(task.id());
            assertEquals(details, task.details());
            assertFalse(task.completed());

            assertEquals(URI.create("http://localhost:8080/api/tasks/" + task.id()),
                    responseEntity.getHeaders().getLocation());

            verify(this.taskRepository).save(task);
        } else {
            assertInstanceOf(Task.class, responseEntity.getBody());
        }
        verifyNoMoreInteractions(this.taskRepository);
    }

    @Test
    void handleCreateNewTask_PayloadIsInvalid_ReturnValidResponseEntity() {
        // given
        var details = "   ";
        var locale = Locale.US;
        var errorMessage = "Details is empty";

        doReturn(errorMessage).when(this.messageSource)
                .getMessage("task.create.details.error.not_set", new Object[0], locale);

        // when
        var responseEntity = this.controller.handleCreateNewTask(new NewTaskPayload(details),
                UriComponentsBuilder.fromUriString("http://localhost:8080"), locale);

        // then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertEquals(new ErrorPresentation(List.of(errorMessage)), responseEntity.getBody());

        verifyNoInteractions(taskRepository);
    }
}
