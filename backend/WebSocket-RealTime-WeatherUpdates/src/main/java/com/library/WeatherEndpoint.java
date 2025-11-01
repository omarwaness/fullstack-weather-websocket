package com.library;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

@ServerEndpoint("/weather")
public class WeatherEndpoint {

    private static final Set<Session> sessions = new HashSet<>();
    private static final Map<Session, Set<String>> subscriptions = new HashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected: " + session.getId());
        synchronized (sessions) {
            sessions.add(session);
        }
        synchronized (subscriptions) {
            subscriptions.put(session, new HashSet<>());
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            JSONObject json = new JSONObject(message);
            String action = json.optString("action");
            String city = json.optString("city");

            synchronized (subscriptions) {
                Set<String> cities = subscriptions.get(session);
                if (cities == null) return;

                switch (action) {
                    case "subscribe":
                        cities.add(city);
                        //session.getBasicRemote().sendText("Subscribed to " + city);
                        break;
                    case "unsubscribe":
                        cities.remove(city);
                        //session.getBasicRemote().sendText("Unsubscribed from " + city);
                        break;
                    default:
                        session.getBasicRemote().sendText("Unknown action: " + action);
                }
            }
        } catch (Exception e) {
            try {
                session.getBasicRemote().sendText("Invalid message format");
            } catch (IOException ignored) {}
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Disconnected: " + session.getId());
        synchronized (sessions) {
            sessions.remove(session);
        }
        synchronized (subscriptions) {
            subscriptions.remove(session);
        }
    }

    public static Map<Session, Set<String>> getSubscriptions() {
        synchronized (subscriptions) {
            return new HashMap<>(subscriptions);
        }
    }

    public static void sendToSession(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            System.err.println("Error sending to " + session.getId() + ": " + e.getMessage());
        }
    }
}
