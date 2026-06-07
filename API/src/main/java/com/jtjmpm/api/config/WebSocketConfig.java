package com.jtjmpm.api.config;

import com.google.gson.Gson;
import com.jtjmpm.api.controller.MessageDispatcher;
import com.jtjmpm.api.websocket.GameHandler;
import com.jtjmpm.api.service.GameStateStore;
import com.jtjmpm.api.service.SessionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import java.net.InetSocketAddress;

@Configuration
@EnableWebSocket
public class WebSocketConfig {
    @Bean(destroyMethod = "stop")
    public GameHandler gameHandler(GameStateStore store,
                                   SessionRegistry registry,
                                   MessageDispatcher dispatcher) {
        GameHandler gameHandler = new GameHandler(
                new InetSocketAddress("0.0.0.0", 3000),
                registry,
                store,
                dispatcher
        );
        gameHandler.start();
        return gameHandler;
    }

    @Bean
    public Gson gson() { return new Gson(); }
}
