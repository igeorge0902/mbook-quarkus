package com.jeet.broadcasting;

import jakarta.websocket.OnClose;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.PongMessage;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.logging.Logger;

@ServerEndpoint("/ws")
public class WebSocketServer {
    private static final Logger LOG = Logger.getLogger(WebSocketServer.class);
    private static final ConcurrentHashMap<String, Set<Session>> byDevice = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        String deviceId = session.getRequestParameterMap()
                .getOrDefault("deviceId", List.of()).stream().findFirst().orElse("");
        if (deviceId == null || deviceId.isBlank()) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "deviceId required"));
            return;
        }
        session.setMaxIdleTimeout(0); // Disable idle timeout; rely on client pings
        session.getUserProperties().put("deviceId", deviceId);
        byDevice.compute(deviceId, (key, sessions) -> {
            if (sessions == null) {
                sessions = ConcurrentHashMap.newKeySet();
            }
            sessions.add(session);
            return sessions;
        });
        System.out.println("New connection: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        remove(session);
        System.out.println("Connection closed: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        remove(session);
        LOG.error("WebSocket error for session " + session.getId(), throwable);
    }

    @OnMessage
    public void onPong(PongMessage pong, Session session) {
        // Client ping/pong keepalive — no action needed, just keeps the connection alive
    }

    public static Set<String> registeredDevices() {
        return Set.copyOf(byDevice.keySet());
    }

    public static void sendToDevice(String deviceId, String message) {
        for (Session session : byDevice.getOrDefault(deviceId, Set.of())) {
            if (!session.isOpen()) {
                remove(session);
                continue;
            }
            try {
                synchronized (session) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException | RuntimeException e) {
                remove(session);
                LOG.error("Removed failed WebSocket session " + session.getId(), e);
            }
        }
    }

    // Kept for the existing user-notification Kafka listener.
    public static void broadcastMessage(String message) {
        registeredDevices().forEach(deviceId -> sendToDevice(deviceId, message));
    }

    private static void remove(Session session) {
        Object deviceId = session.getUserProperties().get("deviceId");
        if (deviceId instanceof String device) {
            byDevice.computeIfPresent(device, (key, sessions) -> {
                sessions.remove(session);
                return sessions.isEmpty() ? null : sessions;
            });
        }
    }
}
