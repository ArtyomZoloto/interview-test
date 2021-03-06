package test;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class WeatherLogger {
    public static void main(String[] args) {
        Processor processor = new Processor();
        processor.start();
        System.out.println("Weather logger started.");
    }
}

class Processor {
    private Runnable fetcher;
    private Thread thread;

    public void start() {
        fetcher = new Fetcher();
        thread = new Thread(fetcher);
        thread.run();
    }

    public void stop() {
        thread.interrupt();
    }
}

class Fetcher implements Runnable {
    private String time;
    private String temp;
    private String city;
    private String pressure;
    private String weather;

    public void run() {
        while (true) {
            try {
                URL url = null;
                url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=moscow&appid=c0ac6277e76098696153edfc932e7bae&cnt=1");
                HttpURLConnection connection = null;
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("accept", "application/json");
                InputStream inputStream = connection.getInputStream();
                String responseString = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
                // log
                System.out.println(responseString);
                time = LocalDateTime.now().toString();
                temp = StringUtils.substringBetween(responseString, "\"main\":{\"temp\":", ",\"feels_like\":");
                city = StringUtils.substringBetween(responseString, "\"name\":\"", "\",\"coord\"");
                pressure = StringUtils.substringBetween(responseString, "\"pressure\":", ",\"sea_level\"");
                weather = StringUtils.substringBetween(responseString, "\"main\":", "\",\"description\"").split("\"main\":\"")[1];
                Saver saver = new Saver();
                saver.save(time, city, temp, pressure, weather);
                Thread.sleep(10_000);
            } catch (Exception e) {
                throw new NullPointerException("cant get data!");
            }
        }
    }
}

class Saver {

    void save(String time, String city, String temp, String pressure, String weather) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("cant find driver!");
        }
        try {
            conn = DriverManager.getConnection("jdbc:mysql://sql2.freesqldatabase.com:3306/sql2391769", "sql2391769", "nD2!kJ7*");

            Statement stmt = null;
            String query = "INSERT INTO `WEATHER_LOG` (`time`, `city`, `temp`, `pressure`, `weather`) " +
                    "VALUES ('" + time + "', '" + city + "', '" + temp + "', '" + pressure + "', '" + weather + "');";
            System.out.println(query);
            try {
                stmt = conn.createStatement();
                int result = stmt.executeUpdate(query);
                //log
                System.out.println("records added: " + result);
            } catch (SQLException e) {
                throw new Error("Problem", e);
            }
        } catch (SQLException e) {
            throw new Error("Problem", e);
        }
    }
}