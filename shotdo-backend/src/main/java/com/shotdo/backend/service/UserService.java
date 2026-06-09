package com.shotdo.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shotdo.backend.entity.User;
import com.shotdo.backend.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 인메모리 세션 스토어 (보안 토큰 -> User ID 매핑)
    // 배포 시 JWT 또는 Redis 세션으로 확장 가능합니다.
    private final ConcurrentHashMap<String, Long> sessionStore = new ConcurrentHashMap<>();

    @Transactional
    public User register(String username, String password, String nickname) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        String hashedPassword = hashPassword(password);
        User user = User.builder()
                .username(username)
                .password(hashedPassword)
                .nickname(nickname)
                .build();

        return userRepository.save(user);
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!user.getPassword().equals(hashPassword(password))) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 로그인 세션 토큰 생성
        String token = UUID.randomUUID().toString();
        sessionStore.put(token, user.getId());
        return token;
    }

    public Optional<User> getUserByToken(String token) {
        Long userId = sessionStore.get(token);
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }

    public void logout(String token) {
        sessionStore.remove(token);
    }

    // SHA-256 비밀번호 해싱 유틸리티
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("비밀번호 해싱 중 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}
