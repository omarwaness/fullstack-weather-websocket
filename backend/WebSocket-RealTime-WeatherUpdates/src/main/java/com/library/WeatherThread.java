package com.library;

import jakarta.websocket.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class WeatherThread extends Thread {
    private final Map<String, WeatherService> defaultCities = new LinkedHashMap<>();
    private final Map<String, WeatherService> extraCities = new LinkedHashMap<>();

    public WeatherThread() {
        defaultCities.put("Sousse", new WeatherService(35.81, 10.63));
        defaultCities.put("New York", new WeatherService(40.71, -74.00));
        extraCities.put("London", new WeatherService(51.50, -0.12));
        extraCities.put("Tokyo", new WeatherService(35.67, 139.76));
    }

    @Override
    public void run() {
        while (true) {
            try {
                Map<String, JSONObject> allWeather = new HashMap<>();

                for (Map.Entry<String, WeatherService> entry : defaultCities.entrySet()) {
                    String city = entry.getKey();
                    JSONObject data = new JSONObject(entry.getValue().fetchWeatherData());
                    data.put("city", city);
                    allWeather.put(city, data);
                }

                for (Map.Entry<String, WeatherService> entry : extraCities.entrySet()) {
                    String city = entry.getKey();
                    JSONObject data = new JSONObject(entry.getValue().fetchWeatherData());
                    data.put("city", city);
                    allWeather.put(city, data);
                }

                Map<Session, Set<String>> subsSnapshot = WeatherEndpoint.getSubscriptions();

                for (Map.Entry<Session, Set<String>> entry : subsSnapshot.entrySet()) {
                    Session session = entry.getKey();
                    Set<String> subscribed = entry.getValue();

                    JSONArray weatherArray = new JSONArray();

                    for (String city : defaultCities.keySet()) {
                        weatherArray.put(allWeather.get(city));
                    }
                    for (String city : subscribed) {
                        if (extraCities.containsKey(city)) {
                            weatherArray.put(allWeather.get(city));
                        }
                    }

                    WeatherEndpoint.sendToSession(session, weatherArray.toString());
                }

                System.out.println("Sent personalized weather arrays to clients.");
                Thread.sleep(60000);

            } catch (InterruptedException e) {
                System.out.println("WeatherThread was interrupted.");
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
