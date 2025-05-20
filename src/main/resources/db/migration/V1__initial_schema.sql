CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    role VARCHAR(20) NOT NULL,
    education_level VARCHAR(20) NOT NULL DEFAULT 'BACHELOR',
    course_number INTEGER NOT NULL DEFAULT 1
);


CREATE TABLE olympiads (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    age INTEGER,
    course VARCHAR(20)
);


CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    task_text TEXT NOT NULL,
    max_score INTEGER NOT NULL,
    task_type VARCHAR(20) NOT NULL,
    correct_answer TEXT,
    olympiad_id BIGINT REFERENCES olympiads(id) ON DELETE CASCADE
);


CREATE TABLE participations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    olympiad_id BIGINT REFERENCES olympiads(id) ON DELETE CASCADE,
    registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE ratings (
    id BIGSERIAL PRIMARY KEY,
    olympiad_id BIGINT REFERENCES olympiads(id) ON DELETE CASCADE,
    total_score REAL
);


CREATE TABLE results (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    olympiad_id BIGINT REFERENCES olympiads(id) ON DELETE CASCADE,
    total_score REAL NOT NULL,
    submission_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    place INTEGER,
    rating_id BIGINT REFERENCES ratings(id) ON DELETE SET NULL
);


CREATE TABLE answers (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    answer_text TEXT,
    score REAL,
    result_id BIGINT REFERENCES results(id) ON DELETE SET NULL,
    submission_date TIMESTAMP NOT NULL
);

CREATE TABLE user_answers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
    answer TEXT NOT NULL,
    score INTEGER,
    submission_time TIMESTAMP NOT NULL
);


