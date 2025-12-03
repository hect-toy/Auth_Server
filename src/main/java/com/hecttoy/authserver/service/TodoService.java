package com.hecttoy.authserver.service;

import com.hecttoy.authserver.dto.CreateTodoRequest;
import com.hecttoy.authserver.dto.TodoResponse;
import com.hecttoy.authserver.dto.UpdateTodoRequest;
import com.hecttoy.authserver.exception.ResourceNotFoundException;
import com.hecttoy.authserver.model.Todo;
import com.hecttoy.authserver.model.User;
import com.hecttoy.authserver.repository.TodoRepository;
import com.hecttoy.authserver.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    public TodoResponse createTodo(String username, CreateTodoRequest request) {
        log.info("Creating todo for user: {}", username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Todo todo = Todo.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .priority(request.getPriority() != null ? request.getPriority() : 0)
            .completed(false)
            .user(user)
            .build();

        Todo savedTodo = todoRepository.save(todo);
        log.info("Todo created with id: {}", savedTodo.getId());

        return mapTodoToResponse(savedTodo);
    }

    public TodoResponse getTodo(String username, Long todoId) {
        log.info("Fetching todo {} for user: {}", todoId, username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Todo todo = todoRepository.findByIdAndUserId(todoId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        return mapTodoToResponse(todo);
    }

    public List<TodoResponse> getAllTodos(String username) {
        log.info("Fetching all todos for user: {}", username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Todo> todos = todoRepository.findByUserId(user.getId());

        return todos.stream()
            .map(this::mapTodoToResponse)
            .collect(Collectors.toList());
    }

    public List<TodoResponse> getCompletedTodos(String username, Boolean completed) {
        log.info("Fetching completed={} todos for user: {}", completed, username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Todo> todos = todoRepository.findByUserIdAndCompleted(user.getId(), completed);

        return todos.stream()
            .map(this::mapTodoToResponse)
            .collect(Collectors.toList());
    }

    public TodoResponse updateTodo(String username, Long todoId, UpdateTodoRequest request) {
        log.info("Updating todo {} for user: {}", todoId, username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Todo todo = todoRepository.findByIdAndUserId(todoId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        if (request.getTitle() != null) {
            todo.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            todo.setDescription(request.getDescription());
        }
        if (request.getCompleted() != null) {
            todo.setCompleted(request.getCompleted());
        }
        if (request.getPriority() != null) {
            todo.setPriority(request.getPriority());
        }

        Todo updatedTodo = todoRepository.save(todo);
        log.info("Todo {} updated", todoId);

        return mapTodoToResponse(updatedTodo);
    }

    public void deleteTodo(String username, Long todoId) {
        log.info("Deleting todo {} for user: {}", todoId, username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Todo todo = todoRepository.findByIdAndUserId(todoId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        todoRepository.delete(todo);
        log.info("Todo {} deleted", todoId);
    }

    private TodoResponse mapTodoToResponse(Todo todo) {
        return TodoResponse.builder()
            .id(todo.getId())
            .title(todo.getTitle())
            .description(todo.getDescription())
            .completed(todo.getCompleted())
            .priority(todo.getPriority())
            .createdAt(todo.getCreatedAt())
            .updatedAt(todo.getUpdatedAt())
            .build();
    }
}
