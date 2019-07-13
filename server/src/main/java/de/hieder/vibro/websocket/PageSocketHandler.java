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
import java.util.HashMap;
import java.util.HashSet;

@Component
public class PageSocketHandler extends TextWebSocketHandler {

    Gson gson = new Gson();

    @Autowired
    protected SmartphoneSocketHandler smartphoneHandler;

    HashSet<WebSocketSession> sockets = new HashSet<>();

    @Override
    public void handleTransportError(WebSocketSession session, Throwable throwable) throws Exception {
        System.out.println("error occured at sender " + session);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sockets.remove(session);
        System.out.println(String.format("Session %s closed because of %s", session.getId(), status.getReason()));

        if(sockets.size() == 0){
            smartphoneHandler.send( gson.toJson(new ServerMsg("CHANGE_VIB_STATE","false")) );
        }

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sockets.add(session);
        System.out.println("Connected ... " + session.getId());


    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage jsonTextMessage) throws Exception {

        if(jsonTextMessage.getPayload().equals("UPDATE")){


            //TODO "false" und false muss im Frontend noch angepasst werden!!!!!!!!!!!!
            if(!smartphoneHandler.isConnected()){
                sendAll( gson.toJson( new ServerMsg("SMARTPHONE","false") ) ,null);
            }else{
                sendAll( gson.toJson( new ServerMsg("SMARTPHONE","true") ) ,null);

                smartphoneHandler.send( gson.toJson(new ServerMsg("STATE_CHECK","BLUETOOTH")) );
            }
        }
        else if(jsonTextMessage.getPayload().equals("VIB_CONNECT")){

            ServerMsg msg = new ServerMsg("CONNECT_TO_VIB","true");
            smartphoneHandler.send(gson.toJson( msg ));
        }
        else if(jsonTextMessage.getPayload().equals("VIB_DISCONNECT")){

            ServerMsg msg = new ServerMsg("CONNECT_TO_VIB","false");
            smartphoneHandler.send(gson.toJson( msg ));
        }
        else if(jsonTextMessage.getPayload().equals("VIB_STATE_ENABLE")){

            ServerMsg msg = new ServerMsg("CHANGE_VIB_STATE","true");

            smartphoneHandler.send(gson.toJson( msg ));
            sendAll( gson.toJson( msg ) ,session);
        }
        else if(jsonTextMessage.getPayload().equals("VIB_STATE_DISABLE")){
            ServerMsg msg = new ServerMsg("CHANGE_VIB_STATE","false");

            smartphoneHandler.send(gson.toJson( msg ));
            sendAll( gson.toJson( msg ) ,session);
        }
        else if(jsonTextMessage.getPayload().startsWith("CHANGE_VIB_VAL")){
            String strVal = jsonTextMessage.getPayload().substring("CHANGE_VIB_VAL ".length());
            ServerMsg msg = new ServerMsg("CHANGE_VIB_VAL",strVal);

            smartphoneHandler.send(gson.toJson( msg ));
            sendAll( gson.toJson( msg ) ,session);
        }



        System.out.println("message received: " + jsonTextMessage.getPayload());

    }

    public void sendAll(String msg,WebSocketSession ses){
        for(WebSocketSession socket : sockets){

            if(ses != null){
                if(socket == ses){
                    continue;
                }
            }

            try {
                socket.sendMessage(new TextMessage(msg));
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}