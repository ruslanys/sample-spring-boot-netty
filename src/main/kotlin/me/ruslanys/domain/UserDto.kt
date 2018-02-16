package me.ruslanys.domain

import org.springframework.util.DigestUtils

class UserDto(firstName: String, lastName: String, email: String) {

    val name: String = "$firstName $lastName"
    val emailHash: String = DigestUtils.md5DigestAsHex(email.toByteArray())

}