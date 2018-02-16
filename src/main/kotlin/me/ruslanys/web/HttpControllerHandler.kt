package me.ruslanys.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.buffer.Unpooled.wrappedBuffer
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN
import io.netty.util.AsciiString
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@ChannelHandler.Sharable
@Component
class HttpControllerHandler(private val pathHandlerProvider: PathHandlerProvider) : SimpleChannelInboundHandler<FullHttpRequest>() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
        private val jacksonObjectMapper = jacksonObjectMapper()
    }

    override fun channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        var responseStatus = HttpResponseStatus.OK
        var responseBody = ""
        var mimeType = APPLICATION_JSON

        try {
            val handler = pathHandlerProvider.getHandler(request)
            if (handler == null) {
                writeResponse(ctx, HttpResponseStatus.NOT_FOUND, TEXT_PLAIN, "Not found.")
                return
            }

            val response = handler(request)

            if (response is String) {
                responseBody = response
            } else if (response != null) {
                responseBody = toJson(response)
            }
        } catch (e: Exception) {
            responseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR
            responseBody = e.message ?: ""
            mimeType = TEXT_PLAIN
        }

        writeResponse(ctx, responseStatus, mimeType, responseBody)
    }

    private fun toJson(any: Any): String {
        return jacksonObjectMapper.writeValueAsString(any)
    }

    private fun writeResponse(ctx: ChannelHandlerContext, status: HttpResponseStatus, mimeType: AsciiString, body: String) {
        val buf = wrappedBuffer(body.toByteArray())
        val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf)

        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes())
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeType.toString() + "; charset=UTF-8")
        HttpUtil.setKeepAlive(response, true)

        ctx.writeAndFlush(response)
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Something went wrong", cause)
        writeResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, TEXT_PLAIN, cause.message ?: "")
    }



}