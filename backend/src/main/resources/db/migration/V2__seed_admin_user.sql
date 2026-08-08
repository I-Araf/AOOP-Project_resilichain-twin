-- Dev-only seed admin user. Credentials documented in backend/README.md.
-- email: admin@resilichain.com / password: ChangeMe123!
INSERT INTO app_user (name, email, password_hash, role, active)
VALUES (
    'System Administrator',
    'admin@resilichain.com',
    '$2a$10$AiAOElyo.K/dPYLRteKqb.aLE5qEuuK18Pq5GWmObK/Kt.GWvBzwS',
    'ADMIN',
    TRUE
);
