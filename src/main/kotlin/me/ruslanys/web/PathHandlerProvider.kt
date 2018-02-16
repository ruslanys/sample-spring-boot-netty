package me.ruslanys.web

import io.netty.handler.codec.http.FullHttpRequest
import me.ruslanys.annotation.RequestMapping
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import java.util.*
import javax.annotation.PostConstruct
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod

@Component
class PathHandlerProvider(private val context: ApplicationContext) {

    private val storage = TreeMap<String, Function1<FullHttpRequest, Any?>>()

    @PostConstruct
    private fun init() {
        val beans = context.getBeansWithAnnotation(Controller::class.java).values
        for (bean in beans) {
            val functions = bean::class.declaredFunctions

            for (function in functions) {
                val pathAnnotation = function.findAnnotation<RequestMapping>() ?: continue
                val path = pathAnnotation.value

                val parameters = function.valueParameters
                val parameter = parameters.first()
                if (parameters.size > 1 || !parameter.type.isSubtypeOf(FullHttpRequest::class.createType())) {
                    throw IllegalStateException("Incorrect parameter type of " +
                            "${function.javaMethod!!.declaringClass.name}.${function.name}")
                }

                if (storage.containsKey(pathAnnotation.value)) {
                    throw IllegalStateException("Mapping $path is already exists.")
                } else {
                    storage[path] = { request ->
                        function.call(bean, request)
                    }
                }
            }
        }
    }

    fun getHandler(request: FullHttpRequest) : ((FullHttpRequest) -> Any?)? {
        for (entry in storage) {
            if (request.uri().startsWith(entry.key)) {
                return entry.value
            }
        }

        return null
    }

}