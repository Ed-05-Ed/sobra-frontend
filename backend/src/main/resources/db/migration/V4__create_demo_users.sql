CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       name VARCHAR(120) NOT NULL,
                       email VARCHAR(180) NOT NULL,
                       CONSTRAINT uk_users_email UNIQUE (email)
);

INSERT INTO users (id, name, email)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Favian', 'favian@sobra.demo'),
    ('22222222-2222-2222-2222-222222222222', 'Ana', 'ana@sobra.demo');