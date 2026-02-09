package com.example.todo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.todo.entity.Todo;

@Mapper
public interface TodoMapper {
    List<Todo> selectTodos(
            @Param("q") String q,
            @Param("sort") String sort,
            @Param("dir") String dir);

    List<Todo> selectActiveTodosPaged(
            @Param("q") String q,
            @Param("sort") String sort,
            @Param("dir") String dir,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countActiveTodos(@Param("q") String q);

    List<Todo> selectCompletedLatest(
            @Param("q") String q,
            @Param("limit") int limit);

    Todo selectById(@Param("id") Long id);

    int insert(Todo todo);

    int update(Todo todo);

    int delete(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Todo.Status status, @Param("completedAt") java.time.LocalDateTime completedAt);

    int updatePriority(@Param("id") Long id, @Param("priority") Todo.Priority priority);
}
