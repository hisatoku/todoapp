package com.example.todo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.todo.entity.Todo;
import com.example.todo.entity.Todo.Status;
import com.example.todo.repository.TodoMapper;

@Service
public class TodoService {
    private static final Set<String> ALLOWED_SORTS = Set.of(
            "createdAt",
            "dueDate",
            "title",
            "createdBy",
            "status",
            "priority",
            "completedAt");

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> findAll(String keyword, String sort, String dir) {
        String resolvedSort = resolveSort(sort);
        String resolvedDir = resolveDir(dir);
        String resolvedKeyword = StringUtils.hasText(keyword) ? keyword : null;
        return todoMapper.selectTodos(resolvedKeyword, resolvedSort, resolvedDir);
    }

    public Page<Todo> findActivePage(String keyword, String sort, String dir, int page, int size) {
        String resolvedSort = resolveSort(sort);
        String resolvedDir = resolveDir(dir);
        String resolvedKeyword = StringUtils.hasText(keyword) ? keyword : null;
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        long total = todoMapper.countActiveTodos(resolvedKeyword);
        int offset = safePage * safeSize;
        List<Todo> content = total == 0 ? List.of()
                : todoMapper.selectActiveTodosPaged(resolvedKeyword, resolvedSort, resolvedDir, safeSize, offset);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return new PageImpl<>(content, pageable, total);
    }

    public List<Todo> findCompletedLatest(String keyword, int limit) {
        String resolvedKeyword = StringUtils.hasText(keyword) ? keyword : null;
        int safeLimit = Math.max(limit, 1);
        return todoMapper.selectCompletedLatest(resolvedKeyword, safeLimit);
    }

    public Todo create(Todo todo) {
        normalizeDueDate(todo);
        todo.ensureCreatedAt();
        applyCompletion(todo);
        todoMapper.insert(todo);
        return todo;
    }

    public Todo update(Long id, Todo form) {
        Todo existing = todoMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Todo not found: " + id);
        }
        existing.setTitle(form.getTitle());
        existing.setDetail(form.getDetail());
        existing.setCreatedBy(form.getCreatedBy());
        existing.setDueDate(form.getDueDate());
        existing.setPriority(form.getPriority());
        existing.setStatus(form.getStatus());
        existing.setNoDueDate(form.isNoDueDate());
        normalizeDueDate(existing);
        applyCompletion(existing);
        todoMapper.update(existing);
        return existing;
    }

    public Todo findById(Long id) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null) {
            throw new IllegalArgumentException("Todo not found: " + id);
        }
        return todo;
    }

    public void delete(Long id) {
        todoMapper.delete(id);
    }

    public void updateStatus(Long id, Status status) {
        LocalDateTime completedAt = status == Status.COMPLETED ? LocalDateTime.now() : null;
        todoMapper.updateStatus(id, status, completedAt);
    }

    public void updatePriority(Long id, Todo.Priority priority) {
        todoMapper.updatePriority(id, priority);
    }

    private void normalizeDueDate(Todo todo) {
        if (todo.isNoDueDate()) {
            todo.setDueDate(null);
        }
    }

    private void applyCompletion(Todo todo) {
        if (todo.getStatus() == Status.COMPLETED) {
            if (todo.getCompletedAt() == null) {
                todo.setCompletedAt(LocalDateTime.now());
            }
        } else {
            todo.setCompletedAt(null);
        }
    }

    private String resolveSort(String sort) {
        String resolvedSort = StringUtils.hasText(sort) ? sort : "createdAt";
        if (!ALLOWED_SORTS.contains(resolvedSort)) {
            resolvedSort = "createdAt";
        }
        return resolvedSort;
    }

    private String resolveDir(String dir) {
        return "asc".equalsIgnoreCase(dir) ? "asc" : "desc";
    }
}
