package com.example.todo.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.todo.entity.Todo;
import com.example.todo.entity.Todo.Priority;
import com.example.todo.entity.Todo.Status;
import com.example.todo.service.TodoService;
import org.springframework.data.domain.Page;

@Controller
@RequestMapping("/todos")
public class TodoController {
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping({"", "/"})
    public String list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "dir", required = false) String dir,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            Model model) {
        int pageSize = 15;
        Page<Todo> activePage = todoService.findActivePage(q, sort, dir, page, pageSize);
        List<Todo> completed = todoService.findCompletedLatest(q, 5);
        int total = (int) activePage.getTotalElements();
        int start = total == 0 ? 0 : (activePage.getNumber() * activePage.getSize()) + 1;
        int end = total == 0 ? 0 : Math.min(start + activePage.getNumberOfElements() - 1, total);

        model.addAttribute("todos", activePage.getContent());
        model.addAttribute("completedTodos", completed);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("sort", sort == null ? "" : sort);
        model.addAttribute("dir", dir == null ? "" : dir);
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", Status.values());
        model.addAttribute("page", activePage.getNumber());
        model.addAttribute("totalPages", activePage.getTotalPages());
        model.addAttribute("totalCount", total);
        model.addAttribute("rangeStart", start);
        model.addAttribute("rangeEnd", end);
        return "index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        TodoForm form = new TodoForm();
        form.setPriority(Priority.TODAY);
        form.setStatus(Status.NOT_STARTED);
        prepareForm(model, form, false);
        return "create";
    }

    @PostMapping("/confirm")
    public String confirm(@Valid @ModelAttribute("todo") TodoForm form, BindingResult bindingResult, Model model) {
        validateDueDate(form, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, false);
            return "create";
        }
        model.addAttribute("todo", form);
        return "confirm";
    }

    @PostMapping("/back")
    public String back(@ModelAttribute("todo") TodoForm form, Model model) {
        prepareForm(model, form, false);
        return "create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("todo") TodoForm form, BindingResult bindingResult, Model model) {
        validateDueDate(form, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, false);
            return "create";
        }
        Todo saved = todoService.create(form.toTodo());
        return "redirect:/todos/complete/" + saved.getId();
    }

    @GetMapping("/complete/{id}")
    public String complete(@PathVariable("id") Long id, Model model) {
        Todo todo = todoService.findById(id);
        model.addAttribute("todo", todo);
        return "complete";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Todo todo = todoService.findById(id);
        TodoForm form = TodoForm.fromTodo(todo);
        prepareForm(model, form, true);
        return "create";
    }

    @PostMapping("/{id}/update")
    public String update(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("todo") TodoForm form,
            BindingResult bindingResult,
            Model model) {
        validateDueDate(form, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, true);
            return "create";
        }
        todoService.update(id, form.toTodo());
        return "redirect:/todos";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable("id") Long id,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "dir", required = false) String dir,
            @RequestParam(name = "page", required = false) Integer page) {
        todoService.delete(id);
        return buildRedirect(q, sort, dir, page);
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") Status status,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "dir", required = false) String dir,
            @RequestParam(name = "page", required = false) Integer page) {
        todoService.updateStatus(id, status);
        return buildRedirect(q, sort, dir, page);
    }

    @PostMapping("/{id}/priority")
    public String updatePriority(
            @PathVariable("id") Long id,
            @RequestParam("priority") Priority priority,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "dir", required = false) String dir,
            @RequestParam(name = "page", required = false) Integer page) {
        todoService.updatePriority(id, priority);
        return buildRedirect(q, sort, dir, page);
    }

    private void prepareForm(Model model, TodoForm form, boolean isEdit) {
        model.addAttribute("todo", form);
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", Status.values());
        model.addAttribute("isEdit", isEdit);
    }

    private void validateDueDate(TodoForm form, BindingResult bindingResult) {
        if (!form.isNoDueDate() && form.getDueDate() == null) {
            bindingResult.rejectValue("dueDate", "required", "締切日を設定するか「期限なし」を選択してください");
        }
        if (form.isNoDueDate()) {
            form.setDueDate(null);
        }
    }

    private String buildRedirect(String q, String sort, String dir, Integer page) {
        StringBuilder sb = new StringBuilder("redirect:/todos");
        boolean hasQuery = false;
        if (q != null && !q.isEmpty()) {
            sb.append("?q=").append(q);
            hasQuery = true;
        }
        if (sort != null && !sort.isEmpty()) {
            sb.append(hasQuery ? "&" : "?").append("sort=").append(sort);
            hasQuery = true;
        }
        if (dir != null && !dir.isEmpty()) {
            sb.append(hasQuery ? "&" : "?").append("dir=").append(dir);
            hasQuery = true;
        }
        if (page != null && page >= 0) {
            sb.append(hasQuery ? "&" : "?").append("page=").append(page);
        }
        return sb.toString();
    }
}
