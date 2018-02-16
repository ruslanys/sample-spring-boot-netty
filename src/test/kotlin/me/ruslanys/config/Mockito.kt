package me.ruslanys.config

import org.mockito.Mockito

fun <T> any(clazz: Class<T>): T = Mockito.any<T>(clazz)