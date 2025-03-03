package com.example.weather.service;

import com.example.weather.model.Weather;
import com.example.weather.repository.WeatherRepository;
import com.example.weather.exception.WeatherServiceException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Service
public class WeatherService {
    private final WeatherRepository weatherRepository;
    private final JavaMailSender mailSender;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.alert.email}")
    private String alertEmail;

    @Value("${weather.threshold.temperature:40}")
    private double temperatureThreshold;

    @Value("${weather.threshold.windSpeed:20}")
    private double windSpeedThreshold;

    private final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";

    public WeatherService(WeatherRepository weatherRepository, JavaMailSender mailSender) {
        this.weatherRepository = weatherRepository;
        this.mailSender = mailSender;
    }

    public Weather getWeather(String city) {
        String url = String.format(WEATHER_API_URL, city, apiKey);
        RestTemplate restTemplate = new RestTemplate();
        try {
            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);
            if (response != null && response.getCod() == 200) {
                Weather weather = new Weather(
                        response.getName(),
                        response.getMain().getTemp(),
                        response.getMain().getHumidity(),
                        response.getWind().getSpeed()
                );
                weatherRepository.save(weather);
                checkForExtremeWeather(weather);
                return weather;
            } else {
                throw new WeatherServiceException("Invalid response from weather API: " + (response != null ? response.getMessage() : "Unknown error"));
            }
        } catch (RestClientException e) {
            throw new WeatherServiceException("Failed to fetch weather data", e);
        }
    }

    private void checkForExtremeWeather(Weather weather) {
        if (weather.getTemperature() > temperatureThreshold || weather.getWindSpeed() > windSpeedThreshold) {
            sendWeatherAlert(weather);
        }
    }

    private void sendWeatherAlert(Weather weather) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alertEmail);
            message.setSubject("Extreme Weather Alert: " + weather.getCity());
            message.setText("Alert! Extreme weather conditions detected in " + weather.getCity() +
                    "\nTemperature: " + weather.getTemperature() + "°C" +
                    "\nWind Speed: " + weather.getWindSpeed() + " m/s");
            mailSender.send(message);
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to send email alert", e);
        }
    }

    // Inner classes for API response mapping
    private static class WeatherResponse {
        private String name;
        private MainData main;
        private Wind wind;
        private int cod;
        private String message;
        public String getName() { return name; }
        public MainData getMain() { return main; }
        public Wind getWind() { return wind; }
        public int getCod() { return cod; }
        public String getMessage() { return message; }
    }

    private static class MainData {
        private double temp;
        private int humidity;
        public double getTemp() { return temp; }
        public int getHumidity() { return humidity; }
    }

    private static class Wind {
        private double speed;
        public double getSpeed() { return speed; }
    }
}