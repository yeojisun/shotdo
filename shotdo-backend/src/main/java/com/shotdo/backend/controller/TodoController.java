package com.shotdo.backend.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.shotdo.backend.entity.Todo;
import com.shotdo.backend.entity.User;
import com.shotdo.backend.entity.Verification;
import com.shotdo.backend.service.S3Service;
import com.shotdo.backend.service.TodoService;
import com.shotdo.backend.service.UserService;
import com.shotdo.backend.service.VerificationService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final UserService userService;
    private final TodoService todoService;
    private final VerificationService verificationService;
    private final S3Service s3Service;

    // 공통 유저 검증 유틸리티
    private User authenticate(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("인증 토큰이 누락되었습니다.");
        }
        return userService.getUserByToken(token)
                .orElseThrow(() -> new NoSuchElementException("유효하지 않거나 만료된 세션 토큰입니다."));
    }

    /**
     * 특정 일자의 To-do 목록 및 해당 날짜의 사진 인증 상태 일괄 조회
     */
    @GetMapping
    public ResponseEntity<?> getDayDetails(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            User user = authenticate(token);
            List<Todo> todos = todoService.getTodosByUserAndDate(user, date);
            Optional<Verification> verification = verificationService.getVerificationByUserAndDate(user, date);

            Map<String, Object> response = new HashMap<>();
            response.put("date", date.toString());
            response.put("verified", verification.map(Verification::isVerified).orElse(false));
            response.put("photoUrl", verification.map(Verification::getPhotoUrl).orElse(null));
            response.put("todos", todos.stream().map(t -> {
                Map<String, Object> todoMap = new HashMap<>();
                todoMap.put("id", t.getId());
                todoMap.put("text", t.getText());
                todoMap.put("completed", t.isCompleted());
                return todoMap;
            }).collect(Collectors.toList()));

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 달력 렌더링용: 월별(또는 특정 기간) 전체 인증 스냅샷 데이터 조회
     */
    @GetMapping("/calendar")
    public ResponseEntity<?> getMonthlyCalendarData(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            User user = authenticate(token);
            
            // 데이터 수집
            Map<String, Map<String, Object>> calendarData = new HashMap<>();
            
            LocalDate cursor = startDate;
            while (!cursor.isAfter(endDate)) {
                List<Todo> todos = todoService.getTodosByUserAndDate(user, cursor);
                Optional<Verification> verification = verificationService.getVerificationByUserAndDate(user, cursor);
                
                if (!todos.isEmpty() || verification.isPresent()) {
                    Map<String, Object> dayInfo = new HashMap<>();
                    dayInfo.put("hasTodos", !todos.isEmpty());
                    dayInfo.put("verified", verification.map(Verification::isVerified).orElse(false));
                    dayInfo.put("photoUrl", verification.map(Verification::getPhotoUrl).orElse(null));
                    calendarData.put(cursor.toString(), dayInfo);
                }
                cursor = cursor.plusDays(1);
            }
            
            return ResponseEntity.ok(calendarData);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 새로운 투두 등록
     */
    @PostMapping
    public ResponseEntity<?> addTodo(
            @RequestHeader("Authorization") String token,
            @RequestBody TodoAddRequest request) {
        try {
            User user = authenticate(token);
            Todo todo = todoService.addTodo(user, request.getText(), request.getDate());
            return ResponseEntity.status(HttpStatus.CREATED).body(todo);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 투두 완료 여부 토글 (완료 <-> 미완료)
     */
    @PatchMapping("/{todoId}/toggle")
    public ResponseEntity<?> toggleTodo(
            @RequestHeader("Authorization") String token,
            @PathVariable Long todoId) {
        try {
            User user = authenticate(token);
            Todo todo = todoService.toggleTodo(user, todoId);
            return ResponseEntity.ok(todo);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 투두 삭제
     */
    @DeleteMapping("/{todoId}")
    public ResponseEntity<?> deleteTodo(
            @RequestHeader("Authorization") String token,
            @PathVariable Long todoId) {
        try {
            User user = authenticate(token);
            todoService.deleteTodo(user, todoId);
            return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * S3 사진 업로드용 임시 Pre-signed URL 생성 요청
     */
    @GetMapping("/presigned-url")
    public ResponseEntity<?> getPresignedUrl(
            @RequestHeader("Authorization") String token,
            @RequestParam String extension) {
        try {
            authenticate(token);
            String url = s3Service.generatePresignedUrl(extension);
            return ResponseEntity.ok(Map.of("uploadUrl", url));
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 하루 완료 사진 인증 최종 제출 및 달력 채우기
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyDay(
            @RequestHeader("Authorization") String token,
            @RequestBody VerificationRequest request) {
        try {
            User user = authenticate(token);
            Verification verification = verificationService.verifyDay(user, request.getDate(), request.getPhotoUrl());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("date", verification.getDate().toString());
            response.put("photoUrl", verification.getPhotoUrl());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Data
    public static class TodoAddRequest {
        private String text;
        private LocalDate date;
    }

    @Data
    public static class VerificationRequest {
        private LocalDate date;
        private String photoUrl;
    }
}
