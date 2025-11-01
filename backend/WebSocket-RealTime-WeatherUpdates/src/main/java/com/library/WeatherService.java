package com.library;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class WeatherService {
    private final String apiKey = "e97841e0253ecb7100e6b07ce2c872b7";
    private final double latitude;
    private final double longitude;
    private final String BASE_URL_CURRENT = "https://api.openweathermap.org/data/2.5/weather";
    private final String BASE_URL_FORECAST = "https://api.openweathermap.org/data/2.5/forecast";

    public WeatherService(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private String fetchCurrentWeather() throws Exception {
        String apiUrl = String.format("%s?lat=%.4f&lon=%.4f&appid=%s&units=metric",
                BASE_URL_CURRENT, latitude, longitude, apiKey);
        return fetchFromUrl(apiUrl);
    }

    private String fetchForecast() throws Exception {
        String apiUrl = String.format("%s?lat=%.4f&lon=%.4f&appid=%s&units=metric",
                BASE_URL_FORECAST, latitude, longitude, apiKey);
        return fetchFromUrl(apiUrl);
    }

    private String fetchFromUrl(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int status = connection.getResponseCode();
        if (status != 200) {
            return String.format("{\"error\":\"Failed to fetch data\",\"status\":%d}", status);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.lines().collect(Collectors.joining());
        } finally {
            connection.disconnect();
        }
    }

    public String fetchWeatherData() {
        try {
            String current = fetchCurrentWeather();
            String forecast = fetchForecast();

            return String.format("{\"current\":%s,\"forecast\":%s}", current, forecast);

        } catch (Exception e) {
            e.printStackTrace();
            return String.format("{\"error\":\"%s\"}", e.getMessage());
        }
    }
}
