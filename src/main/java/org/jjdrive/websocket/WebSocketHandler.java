package org.jjdrive.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{userId}")
@ApplicationScoped
public class WebSocketHandler {

    // Mapa de sesiones WebSocket activas
    private final Map<String, Session> connectedClients = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        connectedClients.put(userId, session);
        System.out.println("Cliente conectado: " + userId);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        connectedClients.remove(userId);
        System.out.println("Cliente desconectado: " + userId);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        System.out.println("Mensaje recibido de " + userId + ": " + message);
    }

    public void enviarMensaje(String userId, String mensaje) {
        Session session = connectedClients.get(userId);
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(mensaje);
        }
    }
}
