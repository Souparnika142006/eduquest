package com.eduquest.service;

import com.eduquest.dto.*;
import com.eduquest.model.User;
import com.eduquest.repository.UserRepository;
import com.eduquest.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final LeaderboardService leaderboardService;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String BLOCKLIST_PREFIX     = "blocklist:";

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .collegeName(request.getCollegeName())
                .branch(request.getBranch())
                .yearOfStudy(request.getYearOfStudy())
                .build();

        user = userRepository.save(user);

        // Add to leaderboard with 0 XP
        leaderboardService.updateUserScore(user.getId(), 0);

        log.info("New user registered: {} ({})", user.getName(), user.getEmail());
        return generateTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.getIsActive()) throw new BadCredentialsException("Account is deactivated");
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new BadCredentialsException("Invalid email or password");

        log.info("User logged in: {}", user.getEmail());
        return generateTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String userId = jwtProvider.getUserIdFromToken(refreshToken);
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (!refreshToken.equals(storedToken)) {
            throw new BadCredentialsException("Refresh token mismatch — please login again");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        return generateTokens(user);
    }

    public void logout(String accessToken) {
        String userId = jwtProvider.getUserIdFromToken(accessToken);
        // Blocklist access token (expires in 24h matching JWT expiry)
        redisTemplate.opsForValue().set(BLOCKLIST_PREFIX + accessToken, userId, 24, TimeUnit.HOURS);
        // Remove refresh token
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.info("User {} logged out", userId);
    }

    private AuthResponse generateTokens(User user) {
        String accessToken  = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        // Store refresh token in Redis (7 days TTL)
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        return AuthResponse.builder()
                .accessToken(accessToken).refreshToken(refreshToken)
                .userId(user.getId()).name(user.getName())
                .email(user.getEmail()).level(user.getLevel())
                .totalXp(user.getTotalXp()).role(user.getRole().name())
                .build();
    }
}
