CREATE TABLE feedback_message (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    role VARCHAR(255),
    topic VARCHAR(255),
    message TEXT,
    created_at TIMESTAMP,
    admin_reply TEXT,
    admin_reply_at TIMESTAMP
); 