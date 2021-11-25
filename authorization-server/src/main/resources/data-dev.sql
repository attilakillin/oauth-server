-- Creates a user with the following credentials: username: "test", password: "password" --
INSERT INTO user_info (id, name, email, address) VALUES (0, '', '', '');

INSERT INTO user (id, username, password, roles, info_id)
VALUES ('testuserid', 'test', '$2a$10$cpZUhePqKGznFIeKxsiQOeXV8y5fkTjvNTLUs1EFLZ2N//7x1IncO', 'USER', 0);

-- Creates a client instance with the following parameters --
INSERT INTO client (id, secret, redirect_uris, token_endpoint_auth_method, grant_types, response_types,
                    scope, id_issued_at, secret_expires_at, registration_access_token)
VALUES (
    'clientid',
    'clientsecret',
    'http://localhost:8082/callback',
    'client_secret_basic',
    'authorization_code',
    'code',
    'read,write,openid,profile,email,address',
    '2021-10-16 09:45:00',
    NULL,
    'regaccesstoken'
);
