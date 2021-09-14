let userAuthToken = undefined

window.addEventListener("load", function () {
    userAuthToken = sessionStorage.getItem("oauth-user-session-token")
    if (!userAuthToken) {
        window.location.href = window.location.origin + "/user/login"
    }

    const request = new XMLHttpRequest();
    request.open('POST', '/user/validate', true);
    request.send(new URLSearchParams({"userToken": userAuthToken}));
    request.onreadystatechange = function () {
        if (this.readyState === 4 && request.status !== 200) {
            window.location.href = window.location.origin + "/user/login"
        }
    }
}, false)