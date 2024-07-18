package org.kirill.space.cell.simple_rest_service.model;

import java.util.UUID;

public record Task(UUID id, String details, boolean completed) {

    public Task(String details) {
        this(UUID.randomUUID(), details, false);
    }
}
