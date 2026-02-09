package com.example.todo.controller;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.example.todo.entity.Todo;
import com.example.todo.entity.Todo.Priority;
import com.example.todo.entity.Todo.Status;

public class TodoForm {
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String author;

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 500, message = "500文字以内で入力してください")
    private String detail;

    private LocalDate dueDate;

    private boolean noDueDate;

    private Priority priority;

    private Status status;

    public Todo toTodo() {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setCreatedBy(author);
        todo.setTitle(title);
        todo.setDetail(detail);
        todo.setDueDate(dueDate);
        todo.setNoDueDate(noDueDate);
        todo.setPriority(priority);
        todo.setStatus(status);
        return todo;
    }

    public static TodoForm fromTodo(Todo todo) {
        TodoForm form = new TodoForm();
        form.setId(todo.getId());
        form.setAuthor(todo.getCreatedBy());
        form.setTitle(todo.getTitle());
        form.setDetail(todo.getDetail());
        form.setDueDate(todo.getDueDate());
        form.setNoDueDate(todo.getDueDate() == null);
        form.setPriority(todo.getPriority());
        form.setStatus(todo.getStatus());
        return form;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isNoDueDate() {
        return noDueDate;
    }

    public void setNoDueDate(boolean noDueDate) {
        this.noDueDate = noDueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
