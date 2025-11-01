package com.library;

import org.glassfish.tyrus.server.Server;

public class WeatherServer {

    public static void main(String[] args) {
        Server server = new Server("localhost", 8080, "/ws", null, WeatherEndpoint.class);

        try {
            server.start();
            System.out.println("WebSocket server started on ws://localhost:8080/ws/weather");

            WeatherThread weatherThread = new WeatherThread();
            weatherThread.start();

            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            server.stop();
            System.out.println("Server stopped");
        }
    }
}
