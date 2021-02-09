package test;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class ForecasetLogger {
    public static void main(String[] args) {
        ForecasetLogger forecasetLogger = new ForecasetLogger();
        forecasetLogger.start();
        //log
        System.out.println("Forecast logger started.");
    }

    void start() {
        Fetcher fetcher = new Fetcher();
        Thread thread = new Thread(fetcher);
        thread.run();
    }
}

class Fetcher implements Runnable {

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
                String time = LocalDateTime.now().toString();
                String temp = StringUtils.substringBetween(responseString, "\"main\":{\"temp\":", ",\"feels_like\":");
                String city = StringUtils.substringBetween(responseString, "\"name\":\"", "\",\"coord\"");
                String pressure = StringUtils.substringBetween(responseString, "\"pressure\":", ",\"sea_level\"");
                String weather = StringUtils.substringBetween(responseString, "\"main\":", "\",\"description\"").split("\"main\":\"")[1];
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