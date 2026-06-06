package com.eduquest.service;

import com.eduquest.dto.LeaderboardEntryDto;
import com.eduquest.dto.UserRankDto;
import com.eduquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    private static final String GLOBAL_LB_KEY    = "leaderboard:global";
    private static final String COLLEGE_LB_PREFIX = "leaderboard:college:";
    private static final String SUBJECT_LB_PREFIX = "leaderboard:subject:";

    /** Called after each quiz attempt to update Redis sorted set */
    public void updateUserScore(String userId, int totalXp) {
        var zOps = redisTemplate.opsForZSet();
        zOps.add(GLOBAL_LB_KEY, userId, totalXp);
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getCollegeName() != null)
                zOps.add(COLLEGE_LB_PREFIX + normalize(user.getCollegeName()), userId, totalXp);
        });
        log.debug("Updated leaderboard for user {} — {} XP", userId, totalXp);
    }

    public void updateSubjectScore(String userId, String subject, int subjectXp) {
        redisTemplate.opsForZSet().add(SUBJECT_LB_PREFIX + normalize(subject), userId, subjectXp);
    }

    public List<LeaderboardEntryDto> getGlobalLeaderboard(int limit) {
        return buildLeaderboard(GLOBAL_LB_KEY, limit);
    }

    public List<LeaderboardEntryDto> getCollegeLeaderboard(String userId, int limit) {
        return userRepository.findById(userId)
            .filter(u -> u.getCollegeName() != null)
            .map(u -> buildLeaderboard(COLLEGE_LB_PREFIX + normalize(u.getCollegeName()), limit))
            .orElse(List.of());
    }

    public List<LeaderboardEntryDto> getSubjectLeaderboard(String subject, int limit) {
        return buildLeaderboard(SUBJECT_LB_PREFIX + normalize(subject), limit);
    }

    public UserRankDto getUserRank(String userId) {
        var zOps = redisTemplate.opsForZSet();
        Long rank = zOps.reverseRank(GLOBAL_LB_KEY, userId);
        Double score = zOps.score(GLOBAL_LB_KEY, userId);
        long totalUsers = Optional.ofNullable(zOps.zCard(GLOBAL_LB_KEY)).orElse(0L);

        return UserRankDto.builder()
                .userId(userId)
                .rank(rank != null ? rank + 1 : -1)
                .totalXp(score != null ? score.intValue() : 0)
                .totalParticipants(totalUsers)
                .percentile(rank != null ? Math.round((1 - (double)(rank + 1) / totalUsers) * 100) : 0)
                .build();
    }

    private List<LeaderboardEntryDto> buildLeaderboard(String key, int limit) {
        Set<ZSetOperations.TypedTuple<String>> entries =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);
        if (entries == null) return List.of();

        AtomicLong rank = new AtomicLong(1);
        return entries.stream().map(e -> {
            String uid = e.getValue();
            int xp = e.getScore() != null ? e.getScore().intValue() : 0;
            return userRepository.findById(uid)
                .map(u -> LeaderboardEntryDto.builder()
                    .rank(rank.getAndIncrement()).userId(uid)
                    .name(u.getName()).collegeName(u.getCollegeName())
                    .branch(u.getBranch()).avatarEmoji(u.getAvatarEmoji())
                    .totalXp(xp).level(u.getLevel()).streakCount(u.getStreakCount())
                    .build())
                .orElse(null);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static String normalize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]", "_");
    }
}
