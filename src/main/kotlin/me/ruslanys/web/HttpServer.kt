package me.ruslanys.web

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PreDestroy


@Component
class HttpServer(@Value("\${server.port:8080}") port: Int,
                 private val httpChannelInitializer: HttpChannelInitializer) : CommandLineRunner {

    companion object {
        private val log = LoggerFactory.getLogger(HttpServer::class.java)
    }

    private val bossGroup = EpollEventLoopGroup(1)
    private val workerGroup = EpollEventLoopGroup() // 12?
    private val port: Int

    init {
        if (port == -1) {
            this.port = Random().nextInt(65_535)
        } else {
            this.port = port
        }
    }


    override fun run(vararg args: String?) {
        try {
            val sb = ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(EpollServerSocketChannel::class.java)
                    .childHandler(httpChannelInitializer)
                    .option(ChannelOption.SO_BACKLOG, 512)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)

            val future = sb.bind(port)
            log.info("Server is started on {} port.", port)

            future.sync() // locking the thread until groups are going on
            future.channel().closeFuture().sync()
        } catch (e: InterruptedException) {
            log.error("Something went wrong", e)
        } finally {
            shutdown()
        }
    }

    @PreDestroy
    fun shutdown() {
        log.info("Server is shutting down.")
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }

}
