-- Tạo bảng users
CREATE TABLE IF NOT EXISTS users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    email        VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(20)  NOT NULL DEFAULT 'STUDENT',
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   DATETIME,
    updated_at   DATETIME
);

-- Tạo bảng profile
CREATE TABLE IF NOT EXISTS user_profiles (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    full_name    VARCHAR(100),
    phone        VARCHAR(20),
    gender       VARCHAR(10),
    birth_date   DATE,
    address      TEXT,
    avatar_url   VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS student_profiles (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    student_code    VARCHAR(20) UNIQUE,
    faculty         VARCHAR(100),
    major           VARCHAR(100),
    intake_year     INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tạo bảng course, semester
CREATE TABLE IF NOT EXISTS semesters (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    start_date DATE,
    end_date   DATE
);

CREATE TABLE IF NOT EXISTS courses (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(20) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    credits     INT,
    semester_id BIGINT,
    FOREIGN KEY (semester_id) REFERENCES semesters(id)
);

-- Tạo bảng enrollment
CREATE TABLE IF NOT EXISTS enrollments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    course_id   BIGINT NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    enrolled_at DATETIME,
    FOREIGN KEY (user_id)   REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);