package com.shotdo.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.shotdo.backend.service.S3Service;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final S3Service s3Service;

    /**
     * 오늘의 인증용 Pre-signed URL 발급 요청
     */
    @GetMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@RequestParam String extension) {
        String url = s3Service.generatePresignedUrl(extension);
        Map<String, String> response = new HashMap<>();
        response.put("uploadUrl", url);
        return ResponseEntity.ok(response);
    }

    /**
     * 달력 인증 상태 완료 처리
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyDay(
            @RequestParam String dateKey,
            @RequestParam String s3ImageUrl) {
        
        // TODO: DB 내 해당 일자의 To-do 완료 상태 조회 후,
        // 해당 날짜를 '인증됨' 상태로 갱신하고 S3 파일 URL 저장
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("date", dateKey);
        response.put("photoUrl", s3ImageUrl);
        return ResponseEntity.ok(response);
    }
}
