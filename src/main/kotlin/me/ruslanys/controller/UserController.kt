package me.ruslanys.controller

import io.netty.handler.codec.http.FullHttpRequest
import me.ruslanys.annotation.RequestMapping
import me.ruslanys.domain.UserDto
import org.springframework.stereotype.Controller

@Controller
class UserController {

    @RequestMapping("/api/user/current")
    fun getCurrentUser(request: FullHttpRequest): Any? {
        return UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com")
    }

}