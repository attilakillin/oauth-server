-- Creates a user with the following credentials: username: "test", password: "password" --
INSERT INTO user (id, username, password, roles)
VALUES ('testuserid', 'test', '$2a$10$cpZUhePqKGznFIeKxsiQOeXV8y5fkTjvNTLUs1EFLZ2N//7x1IncO', 'USER');