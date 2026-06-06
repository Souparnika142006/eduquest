# 🎓 EduQuest — Gamified Smart Education Platform
### Smart India Hackathon (SIH) Project

> A full-stack gamified education platform for college students featuring Quiz Engine, XP/Level System, Leaderboard, AI Tutor (Claude), and Progress Analytics.

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React + Vite + Tailwind CSS |
| Backend | Java 17 + Spring Boot 3.2 |
| Database | PostgreSQL 16 |
| Cache / Leaderboard | Redis 7 |
| AI Tutor | Anthropic Claude API (claude-sonnet-4) |
| Auth | JWT (access + refresh tokens) |
| Deployment | Docker + Docker Compose + Nginx |
| API Docs | Swagger UI (SpringDoc OpenAPI 3) |

---

## ✨ Core Features

### 🎮 Gamification Engine
- **XP System** — Earn XP on every quiz (10 / 25 / 50 based on difficulty)
- **Streak Bonus** — Up to +50% XP for consecutive daily activity
- **Levels** — Progressive levelling with titles (Scholar → Grandmaster)
- **Badges** — 12 achievement badges (Speed Demon, Perfect 10, Century, etc.)

### ⚡ Quiz & Challenges
- Multi-difficulty quizzes (Easy / Medium / Hard)
- Timed questions with server-side answer validation
- Daily Challenge — rotates every 24 hours
- Subject filtering (CS, Algorithms, DBMS, OS, Networks, AI/ML, Math)

### 🏆 Leaderboard
- Global, College-level, and Subject-specific boards
- Redis Sorted Sets for O(log n) rank updates
- Shows rank, XP, streak, college, branch

### 🤖 AI Tutor
- Powered by Anthropic Claude (claude-sonnet-4)
- Context-aware conversation history
- System prompt tuned for Indian CS engineering curriculum
- Topic-focused sessions (Data Structures, GATE prep, etc.)

### 📊 Progress Analytics
- Subject-wise mastery percentages
- Weekly XP bar chart
- 90-day activity heatmap
- Badge showcase

---

## 🚀 Quick Start

### Prerequisites
- Java 17+, Maven 3.9+
- Docker & Docker Compose
- Node.js 18+ (for frontend)

### 1. Clone & Configure
```bash
git clone https://github.com/your-team/eduquest.git
cd eduquest

# Set your environment variables
cp .env.example .env
# Edit .env and add:
#   ANTHROPIC_API_KEY=your_key_here
#   JWT_SECRET=your_base64_secret
```

### 2. Start with Docker Compose
```bash
docker-compose up -d
```
This starts PostgreSQL, Redis, Spring Boot backend, and Nginx frontend.

### 3. Access the App
- **Frontend:**  http://localhost:3000
- **Backend API:** http://localhost:8080/api/v1
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs JSON:** http://localhost:8080/api-docs

### 4. Manual Backend Start (Development)
```bash
cd eduquest-backend
mvn spring-boot:run
```

---

## 📡 API Endpoints

### Authentication
```
POST /api/v1/auth/register    — Register new student
POST /api/v1/auth/login       — Login → JWT tokens
POST /api/v1/auth/refresh     — Refresh access token
POST /api/v1/auth/logout      — Invalidate token
```

### Quiz
```
GET  /api/v1/quizzes                     — List quizzes (paginated)
GET  /api/v1/quizzes/{id}               — Quiz detail with questions
POST /api/v1/quizzes/{id}/attempt        — Submit answers → XP + result
GET  /api/v1/quizzes/daily-challenge     — Today's daily quiz
GET  /api/v1/quizzes/subjects            — All available subjects
GET  /api/v1/quizzes/my-attempts         — User's attempt history
```

### Leaderboard
```
GET  /api/v1/leaderboard/global          — Top 100 global
GET  /api/v1/leaderboard/college         — Your college board
GET  /api/v1/leaderboard/subject/{name}  — Subject board
GET  /api/v1/leaderboard/my-rank         — Your rank + percentile
```

### AI Tutor
```
POST /api/v1/ai-tutor/ask                — Ask the AI tutor
GET  /api/v1/ai-tutor/topics             — Available topics
```

### Progress
```
GET  /api/v1/progress/summary            — Overall stats
GET  /api/v1/progress/subjects           — Subject breakdown
GET  /api/v1/progress/weekly-xp          — Last 7 days XP
GET  /api/v1/progress/badges             — Earned badges
GET  /api/v1/progress/heatmap            — 90-day activity heatmap
```

---

## 🗄️ Database Schema (PostgreSQL)

```
users           → id, name, email, college, branch, year, total_xp, level, streak
quizzes         → id, title, subject, difficulty, xp_reward, time_limit
questions       → id, quiz_id, question_text, correct_option_index, explanation
question_options → question_id, option_text
quiz_attempts   → id, user_id, quiz_id, score, xp_earned, time_taken
attempt_answers → attempt_id, selected_option
user_badges     → id, user_id, badge_name, badge_emoji, earned_at
ai_tutor_sessions → id, user_id, topic, message_count, tokens_used
ai_tutor_messages → id, session_id, role, content
```

---

## 🎯 XP & Levelling Formula

```
Base XP per Quiz:
  Easy   → 10 XP × accuracy%
  Medium → 25 XP × accuracy%
  Hard   → 50 XP × accuracy%

Streak Bonus: +10% per streak day (capped at +50%)

Level Threshold: Level N requires N × 500 XP total
  Level 1 → 0 XP
  Level 2 → 500 XP
  Level 5 → 7,500 XP
  Level 10 → 27,500 XP (Scholar badge)
```

---

## 🏅 Badge System

| Badge | Trigger |
|-------|---------|
| 🎉 First Quiz | Complete 1st quiz |
| ⚡ Speed Demon | Finish quiz in < 60s |
| 💯 Perfect 10 | Score 100% |
| 🔥 7-Day Streak | 7 consecutive days |
| 🌟 30-Day Streak | 30 consecutive days |
| 🏅 Quiz Master | 50 quizzes completed |
| 💎 Century | 100 quizzes completed |
| 🔴 Hard Mode | 80%+ on a Hard quiz |
| 📚 Scholar | Reach Level 10 |
| 👑 Grandmaster | Reach Level 25 |

---

## 👥 Team
- **Team Name:** — (SIH 2024)
- **Problem Statement:** Gamified Smart Education for College Students

---

## 📄 License
MIT — Open source for educational purposes.
