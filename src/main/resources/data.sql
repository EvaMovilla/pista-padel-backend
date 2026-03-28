INSERT INTO usuarios (nombre, apellidos, email, password, telefono, rol, fecha_registro, activo)
VALUES (
           'Admin',
           'Sistema',
           'admin@padel.com',
           '$2a$10$FNusajQzQIeSrAjhgBg8aenelornyMjdZKYEs6hKyVWbXAy60UmAy',
           '600000000',
           'ADMIN',
           CURRENT_TIMESTAMP,
           true
       );