package de.hieder.vibro.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketServerConfiguration implements WebSocketConfigurer {

    @Autowired
    protected SmartphoneSocketHandler smartphoneHandler;

    @Autowired
    protected PageSocketHandler pageSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(smartphoneHandler, "/socket/smartphone").setAllowedOrigins("*");
        registry.addHandler(pageSocketHandler, "/socket/page").setAllowedOrigins("*");
    }
}
