package me.bruno.rafael.requests.util;

import me.bruno.rafael.requests.protocol.http.HttpRequestController;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;

@UtilityClass
public class HttpRequestUtil {

    public HttpURLConnection createUrlConnection(Proxy proxy, URI uri) {
        try {
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection(proxy);
            connection.setConnectTimeout(HttpRequestController.DELAY_IN_MILLIS);
            connection.setReadTimeout(HttpRequestController.DELAY_IN_MILLIS);
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            return connection;
        } catch (Exception exception) {
            System.out.println("Error: " + uri);
        }

        return null;
    }

    public BufferedReader getBufferedReader(HttpURLConnection connection) throws IOException {
        return new BufferedReader(new InputStreamReader((connection.getInputStream())));
    }
}
