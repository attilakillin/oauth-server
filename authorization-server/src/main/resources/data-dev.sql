-- Creates a user with the following credentials: username: "test", password: "password" --
INSERT INTO user_info (id, name, email, address) VALUES (0, '', '', '');

INSERT INTO user (id, username, password, roles, info_id)
VALUES ('testuserid', 'test', '$2a$10$cpZUhePqKGznFIeKxsiQOeXV8y5fkTjvNTLUs1EFLZ2N//7x1IncO', 'USER', 0);

-- Creates a client instance with the following parameters --
INSERT INTO client (id, secret, grant_types, response_types, redirect_uris, registration_access_token,
                    scope, id_issued_at, expires_at, token_endpoint_auth_method)
VALUES (
    'clientid',
    'clientsecret',
    'authorization_code',
    'code',
    'http://localhost:8082/callback',
    'regaccesstoken',
    'read,write,openid,profile,email,address',
    '2021-10-16 09:45:00',
    NULL,
    'client_secret_basic'
);
