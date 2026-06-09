package com.shotdo.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shotdo.backend.entity.Todo;
import com.shotdo.backend.entity.User;
import com.shotdo.backend.entity.Verification;
import com.shotdo.backend.repository.TodoRepository;
import com.shotdo.backend.repository.VerificationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final TodoRepository todoRepository;
    private final VerificationRepository verificationRepository;

    public Optional<Verification> getVerificationByUserAndDate(User user, LocalDate date) {
        return verificationRepository.findByUserAndDate(user, date);
    }

    @Transactional
    public Verification verifyDay(User user, LocalDate date, String photoUrl) {
        // 1. 해당 날짜에 투두 리스트가 존재하는지 검증
        List<Todo> todos = todoRepository.findByUserAndDate(user, date);
        if (todos.isEmpty()) {
            throw new IllegalStateException("오늘 등록된 할 일이 없어 인증할 수 없습니다.");
        }

        // 2. 모든 투두가 완료되었는지 검증
        boolean allCompleted = todos.stream().allMatch(Todo::isCompleted);
        if (!allCompleted) {
            throw new IllegalStateException("완료되지 않은 할 일이 있어 사진 인증을 진행할 수 없습니다.");
        }

        // 3. 기존 인증 정보가 있는지 확인
        Optional<Verification> existingOpt = verificationRepository.findByUserAndDate(user, date);
        if (existingOpt.isPresent()) {
            Verification existing = existingOpt.get();
            existing.setPhotoUrl(photoUrl);
            existing.setVerified(true);
            return verificationRepository.save(existing);
        }

        // 4. 신규 인증 생성 및 저장
        Verification verification = Verification.builder()
                .date(date)
                .photoUrl(photoUrl)
                .verified(true)
                .user(user)
                .build();

        return verificationRepository.save(verification);
    }
}
