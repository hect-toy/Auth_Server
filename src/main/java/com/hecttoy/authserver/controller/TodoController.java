package com.hecttoy.authserver.controller;

import com.hecttoy.authserver.dto.CreateTodoRequest;
import com.hecttoy.authserver.dto.StandardResponse;
import com.hecttoy.authserver.dto.TodoResponse;
import com.hecttoy.authserver.dto.UpdateTodoRequest;
import com.hecttoy.authserver.service.TodoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todos")
@Slf4j
public class TodoController {

    @Autowired
    private TodoService todoService;

    @PostMapping
    public ResponseEntity<StandardResponse<TodoResponse>> createTodo(
            @Valid @RequestBody CreateTodoRequest request) {
        log.info("Create todo endpoint called");

        String username = getAuthenticatedUsername();
        TodoResponse todoResponse = todoService.createTodo(username, request);

        StandardResponse<TodoResponse> response = StandardResponse.success(
            HttpStatus.CREATED.value(),
            "Todo created successfully",
            todoResponse
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<StandardResponse<List<TodoResponse>>> getAllTodos() {
        log.info("Get all todos endpoint called");

        String username = getAuthenticatedUsername();
        List<TodoResponse> todos = todoService.getAllTodos(username);

        StandardResponse<List<TodoResponse>> response = StandardResponse.success(
            HttpStatus.OK.value(),
            "Todos retrieved successfully",
            todos
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<TodoResponse>> getTodo(@PathVariable Long id) {
        log.info("Get todo endpoint called for id: {}", id);

        String username = getAuthenticatedUsername();
        TodoResponse todo = todoService.getTodo(username, id);

        StandardResponse<TodoResponse> response = StandardResponse.success(
            HttpStatus.OK.value(),
            "Todo retrieved successfully",
            todo
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter/completed")
    public ResponseEntity<StandardResponse<List<TodoResponse>>> getCompletedTodos(
            @RequestParam Boolean completed) {
        log.info("Get completed todos endpoint called with completed: {}", completed);

        String username = getAuthenticatedUsername();
        List<TodoResponse> todos = todoService.getCompletedTodos(username, completed);

        StandardResponse<List<TodoResponse>> response = StandardResponse.success(
            HttpStatus.OK.value(),
            "Todos filtered successfully",
            todos
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StandardResponse<TodoResponse>> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTodoRequest request) {
        log.info("Update todo endpoint called for id: {}", id);

        String username = getAuthenticatedUsername();
        TodoResponse todoResponse = todoService.updateTodo(username, id, request);

        StandardResponse<TodoResponse> response = StandardResponse.success(
            HttpStatus.OK.value(),
            "Todo updated successfully",
            todoResponse
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteTodo(@PathVariable Long id) {
        log.info("Delete todo endpoint called for id: {}", id);

        String username = getAuthenticatedUsername();
        todoService.deleteTodo(username, id);

        StandardResponse<Void> response = StandardResponse.success(
            HttpStatus.OK.value(),
            "Todo deleted successfully",
            null
        );

        return ResponseEntity.ok(response);
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
