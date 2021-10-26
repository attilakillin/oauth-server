package com.bme.jnsbbk.resourceserver

import com.bme.jnsbbk.resourceserver.resources.UserData
import com.bme.jnsbbk.resourceserver.resources.UserDataRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ResourceManagementController(
    val userDataRepository: UserDataRepository
) {

    @GetMapping
    fun handleRequest(
        @RequestParam("token") token: String?,
        @RequestAttribute username: String,
        model: Model
    ): String {
        val userData = userDataRepository.findByIdOrNull(username)
            ?: userDataRepository.save(UserData(username))
        model.addAttribute("user", userData)
        model.addAttribute("token", token)
        return "resource-content"
    }

    @PostMapping
    fun handleSave(
        @RequestParam("token") token: String?,
        @RequestParam("username") postUsername: String?,
        @RequestParam("notes") notes: String,
        @RequestAttribute username: String,
        model: Model
    ): String {
        if (postUsername != username) return "login-error"

        val userData = UserData(username, notes)
        userDataRepository.save(userData)

        model.addAttribute("user", userData)
        model.addAttribute("token", token)
        model.addAttribute("success", true)
        return "resource-content"
    }
}
