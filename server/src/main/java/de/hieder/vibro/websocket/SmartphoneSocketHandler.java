package de.hieder.vibro.websocket;

import com.google.gson.Gson;
import de.hieder.vibro.entity.ServerMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Component
public class SmartphoneSocketHandler extends TextWebSocketHandler {

    Gson gson = new Gson();

    WebSocketSession session=null;

    @Autowired
    protected PageSocketHandler pageSocketHandler;

    @Override
    public void handleTransportError(WebSocketSession session, Throwable throwable) throws Exception {
        System.out.println("error occured at sender " + session);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        if(session == this.session){
            this.session = null;

            pageSocketHandler.sendAll( gson.toJson(new ServerMsg("SMARTPHONE","false")),null );
        }

        System.out.println(String.format("Session %s closed because of %s", session.getId(), status.getReason()));

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        if(this.session != null){ //only one smartphone!
            session.close();
            return;
        }

        pageSocketHandler.sendAll( gson.toJson(new ServerMsg("SMARTPHONE","true")),null );
        this.session = session;
        System.out.println("Connected ... " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage jsonTextMessage) throws Exception {
        System.out.println("message received: " + jsonTextMessage.getPayload());
        pageSocketHandler.sendAll( jsonTextMessage.getPayload(),null );
    }

    public boolean isConnected(){
        return this.session != null;
    }

    public void send(String msg){
        try {
            session.sendMessage(new TextMessage(msg));
        } catch (IOException e) {
            e.printStackTrace();
            try {
                session.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}