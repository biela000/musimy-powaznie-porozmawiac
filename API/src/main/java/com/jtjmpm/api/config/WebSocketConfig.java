package com.jtjmpm.api.config;

import com.jtjmpm.api.game.GameHandler;
import com.jtjmpm.api.model.GameStateStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import java.net.InetSocketAddress;

@Configuration
@EnableWebSocket
public class WebSocketConfig {
    @Bean(destroyMethod = "stop")
    public GameHandler gameHandler(GameStateStore store) {
        GameHandler gameHandler = new GameHandler(
                new InetSocketAddress("0.0.0.0", 3000),
                store
        );
        gameHandler.start();
        return gameHandler;
    }
}
