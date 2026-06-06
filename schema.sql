-- ══════════════════════════════════════════════════════════════
-- EduQuest Gamified Education Platform — PostgreSQL Schema
-- SIH Project | Spring Boot + React + Redis
-- ══════════════════════════════════════════════════════════════

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ─── USERS ───────────────────────────────────────────────────
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                VARCHAR(50)  NOT NULL,
    email               VARCHAR(100) NOT NULL UNIQUE,
    password            VARCHAR(255) NOT NULL,
    college_name        VARCHAR(150),
    branch              VARCHAR(100),
    year_of_study       SMALLINT CHECK (year_of_study BETWEEN 1 AND 6),
    total_xp            INTEGER      NOT NULL DEFAULT 0,
    level               SMALLINT     NOT NULL DEFAULT 1,
    streak_count        SMALLINT     NOT NULL DEFAULT 0,
    last_active_date    TIMESTAMP,
    role                VARCHAR(20)  NOT NULL DEFAULT 'STUDENT'
                            CHECK (role IN ('STUDENT','INSTRUCTOR','ADMIN')),
    avatar_emoji        VARCHAR(10)  DEFAULT '🎓',
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email       ON users(email);
CREATE INDEX idx_users_college     ON users(college_name);
CREATE INDEX idx_users_total_xp    ON users(total_xp DESC);

-- ─── QUIZZES ─────────────────────────────────────────────────
CREATE TABLE quizzes (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title               VARCHAR(200) NOT NULL,
    subject             VARCHAR(100) NOT NULL,
    description         TEXT,
    difficulty          VARCHAR(10)  NOT NULL CHECK (difficulty IN ('EASY','MEDIUM','HARD')),
    xp_reward           SMALLINT     NOT NULL,
    time_limit_seconds  SMALLINT     DEFAULT 300,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by          UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_quizzes_subject    ON quizzes(subject);
CREATE INDEX idx_quizzes_difficulty ON quizzes(difficulty);

-- ─── QUESTIONS ───────────────────────────────────────────────
CREATE TABLE questions (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quiz_id               UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    question_text         TEXT NOT NULL,
    correct_option_index  SMALLINT NOT NULL CHECK (correct_option_index BETWEEN 0 AND 3),
    explanation           TEXT,
    topic_tag             VARCHAR(80),
    order_index           SMALLINT DEFAULT 0
);

CREATE INDEX idx_questions_quiz ON questions(quiz_id);

-- ─── QUESTION OPTIONS ────────────────────────────────────────
CREATE TABLE question_options (
    question_id  UUID    NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_text  TEXT    NOT NULL,
    option_order SMALLINT DEFAULT 0
);

-- ─── QUIZ ATTEMPTS ───────────────────────────────────────────
CREATE TABLE quiz_attempts (
    id                   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id              UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quiz_id              UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    score                SMALLINT NOT NULL,
    total_questions      SMALLINT NOT NULL,
    xp_earned            SMALLINT NOT NULL DEFAULT 0,
    time_taken_seconds   SMALLINT,
    attempted_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attempts_user        ON quiz_attempts(user_id);
CREATE INDEX idx_attempts_quiz        ON quiz_attempts(quiz_id);
CREATE INDEX idx_attempts_attempted   ON quiz_attempts(attempted_at DESC);

-- ─── ATTEMPT ANSWERS ─────────────────────────────────────────
CREATE TABLE attempt_answers (
    attempt_id       UUID    NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    selected_option  SMALLINT NOT NULL,
    answer_order     SMALLINT DEFAULT 0
);

-- ─── BADGES ──────────────────────────────────────────────────
CREATE TABLE user_badges (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_name          VARCHAR(100) NOT NULL,
    badge_emoji         VARCHAR(10),
    badge_description   VARCHAR(255),
    earned_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_badges_user ON user_badges(user_id);

-- ─── AI TUTOR SESSIONS ───────────────────────────────────────
CREATE TABLE ai_tutor_sessions (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic        VARCHAR(100),
    message_count SMALLINT    NOT NULL DEFAULT 0,
    tokens_used  INTEGER      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ─── AI TUTOR MESSAGES ───────────────────────────────────────
CREATE TABLE ai_tutor_messages (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id  UUID    NOT NULL REFERENCES ai_tutor_sessions(id) ON DELETE CASCADE,
    role        VARCHAR(10) NOT NULL CHECK (role IN ('user','assistant')),
    content     TEXT        NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ─── SEED: SAMPLE QUIZ DATA ──────────────────────────────────
INSERT INTO quizzes (id, title, subject, difficulty, xp_reward, time_limit_seconds) VALUES
  ('a1b2c3d4-0000-0000-0000-000000000001', 'Data Structures Basics', 'Data Structures', 'EASY', 10, 300),
  ('a1b2c3d4-0000-0000-0000-000000000002', 'Algorithm Complexity', 'Algorithms', 'MEDIUM', 25, 300),
  ('a1b2c3d4-0000-0000-0000-000000000003', 'Database Design', 'DBMS', 'MEDIUM', 25, 300),
  ('a1b2c3d4-0000-0000-0000-000000000004', 'OS Process Management', 'Operating Systems', 'HARD', 50, 420);

-- ─── TRIGGERS: auto-update updated_at ────────────────────────
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
  BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_sessions_updated_at
  BEFORE UPDATE ON ai_tutor_sessions
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
