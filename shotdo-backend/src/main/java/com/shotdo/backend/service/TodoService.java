package com.shotdo.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shotdo.backend.entity.Todo;
import com.shotdo.backend.entity.User;
import com.shotdo.backend.repository.TodoRepository;
import com.shotdo.backend.repository.VerificationRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final VerificationRepository verificationRepository;

    public List<Todo> getTodosByUserAndDate(User user, LocalDate date) {
        return todoRepository.findByUserAndDate(user, date);
    }

    @Transactional
    public Todo addTodo(User user, String text, LocalDate date) {
        // 이미 해당 날짜의 사진 인증이 완료되었으면 수정 불가능하게 막습니다.
        boolean isVerified = verificationRepository.findByUserAndDate(user, date)
                .map(v -> v.isVerified())
                .orElse(false);

        if (isVerified) {
            throw new IllegalStateException("이미 사진 인증이 완료된 날짜에는 투두를 추가할 수 없습니다.");
        }

        Todo todo = Todo.builder()
                .text(text)
                .completed(false)
                .date(date)
                .user(user)
                .build();

        return todoRepository.save(todo);
    }

    @Transactional
    public Todo toggleTodo(User user, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("해당 투두를 찾을 수 없습니다."));

        if (!todo.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 인증 여부 검증
        boolean isVerified = verificationRepository.findByUserAndDate(user, todo.getDate())
                .map(v -> v.isVerified())
                .orElse(false);

        if (isVerified) {
            throw new IllegalStateException("이미 사진 인증이 완료된 날짜의 투두는 수정할 수 없습니다.");
        }

        todo.setCompleted(!todo.isCompleted());
        return todoRepository.save(todo);
    }

    @Transactional
    public void deleteTodo(User user, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("해당 투두를 찾을 수 없습니다."));

        if (!todo.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 인증 여부 검증
        boolean isVerified = verificationRepository.findByUserAndDate(user, todo.getDate())
                .map(v -> v.isVerified())
                .orElse(false);

        if (isVerified) {
            throw new IllegalStateException("이미 사진 인증이 완료된 날짜의 투두는 삭제할 수 없습니다.");
        }

        todoRepository.delete(todo);
    }
}
