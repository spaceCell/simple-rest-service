package org.kirill.space.cell.simple_rest_service.repository;

import org.kirill.space.cell.simple_rest_service.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {

    List<Task> findAll();

    void save(Task task);

    Optional<Task> findById(UUID id);
}
