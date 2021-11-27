-- Creates a user with the following credentials: username: "test", password: "password" --

DELETE FROM user WHERE id = 'testuserid';
DELETE FROM user_info WHERE id = 0;

INSERT INTO user_info (id, name, email, address) VALUES (0, '', '', '');
INSERT INTO user (id, username, password, roles, info_id)
VALUES ('testuserid', 'test', '$2a$10$cpZUhePqKGznFIeKxsiQOeXV8y5fkTjvNTLUs1EFLZ2N//7x1IncO', 'USER', 0);
