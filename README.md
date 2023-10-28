# oauth-server - A reference implementation of an OAuth 2 authorization server
Created for the project laboratory subject at BME VIK.

---

A Spring Boot server application written in Kotlin, using JUnit 5 for testing, and Thymeleaf for front-end rendering.

The core functions are implemented and unit tested properly:

* OAuth clients can register themselves using the OAuth Dynamic Client Registration protocol.
* Resource owners can register and authenticate themselves using a browser of their choice.
* Clients can request authorization from the resource owner via authorization URLs. Authorization happens on the front channel, in the browser of the resource owner - the client redirects them to this URL, where they can either approve or deny the request.
* The client receives an authorization code from a successful resource owner authorization, and requests an access token directly from the authorization server.

## Project structure

Components are split into packages based on the feature they are used in:

* `authorization`: Authorization phase, the resource owner actively participates.
* `client`: Client registration logic.
* `config`: Application-specific configuration: token lifespans, scheduled task execution frequency, bean implementations, etc.
* `exceptions`: `ApiException`s, that get mapped to relevant HTTP responses.
* `jwt`: JSON Web Token specific classes.
* `token`: Token phase, where the client communicates with the server directly.
* `users`: Resource owner registration and authentication.
* `utils`: Useful Spring and Kotlin classes and extension functions.

Using this structure, the project can be understood package-by-package.

## Endpoints

* `/authorize`: Authorization phase endpoint, visit from a browser with relevant query parameters.
* `/register`: Client registration endpoint, use direct HTTP messages.
* `/token`: Token phase endpoint, use direct HTTP messages.
* `/user/register`: Resource owner registration, visit from a browser.
* `/user/login`: Resource owner authentication, usually pages requiring authentication redirect here.
* `/user/validate`: Validates resource owner credentials and issues JWT tokens that can be stored safely in browser storage.
