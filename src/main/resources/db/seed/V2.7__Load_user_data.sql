-- Insert admin user data
-- Password hash for 'adminpassword' using BCrypt
INSERT INTO user_login (username, password_hash, person_id, created_by, created_at) 
VALUES (
    'admin', 
    '$2a$10$Po09k7ClojpSxNFe/EhbquKoGQ69AU8H.yaPsRJb/D4FXUkB4jpx.',  -- bcrypt hash of 'adminpassword'
    1,
    'system',
    NOW()
);

-- Assign ROLE_ADMIN to admin user
INSERT INTO user_role (user_id, role_id, assigned_at, created_by, created_at)
VALUES (
    (SELECT id FROM user_login WHERE username = 'admin'),
    (SELECT id FROM role WHERE name = 'ROLE_ADMIN'),
    NOW(),
    'system',
    NOW()
);