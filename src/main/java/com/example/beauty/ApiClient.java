
package com.example.beauty;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.io.IOException;

public class ApiClient {

    private static final String SERVER_URL =
            "http://127.0.0.1:8080";

    private static final HttpClient client =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

    private static String token;
    private static String currentUsername;

    // Реєстрація через сервер
    public static void register(
            String username,
            String phone,
            String email,
            String password) {

        String body =
                "username=" + encode(username)
                        + "&phone=" + encode(phone)
                        + "&email=" + encode(email)
                        + "&password=" + encode(password);

        sendPost("/register", body);
    }

    // Авторизація через сервер
    public static void login(String login, String password) {

        String body =
                "login=" + encode(login)
                        + "&password=" + encode(password);

        String response = sendPost("/login", body);

        String[] parts = response.split("\n", 2);

        if (parts.length != 2) {

            throw new IllegalStateException(
                    "Некоректна відповідь сервера!"
            );
        }

        token = parts[0];
        currentUsername = parts[1];
    }

    // Отримання імені користувача
    public static String getCurrentUser() {
        return currentUsername;
    }

    // Вихід із системи
    public static void logout() {

        if (token != null) {
            sendPost("/logout", "");
        }

        token = null;
        currentUsername = null;
    }

    // Перевірка з'єднання
    public static boolean checkConnection() {

        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() == 200;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            return false;

        } catch (Exception e) {
            return false;
        }
    }

    // Передавання POST-запиту
    private static String sendPost(String path, String body) {

        try {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(URI.create(SERVER_URL + path))
                            .timeout(Duration.ofSeconds(10))
                            .header(
                                    "Content-Type",
                                    "application/x-www-form-urlencoded; charset=UTF-8"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(body)
                            );

            if (token != null) {
                builder.header("Authorization", token);
            }

            HttpResponse<String> response = client.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8
                    )
            );

            if (response.statusCode() >= 400) {

                throw new IllegalArgumentException(
                        response.body()
                );
            }

            return response.body();

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Не вдалося підключитися до сервера!", e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Запит перервано!", e
            );
        }
    }

    // Кодування даних для HTTP-запиту
    private static String encode(String value) {

        return URLEncoder.encode(
                value, StandardCharsets.UTF_8
        );
    }

    // Тестування серверної авторизації
    public static void main(String[] args) {

        if (!checkConnection()) {

            System.out.println("Сервер недоступний!");
            return;
        }

        System.out.println("З'єднання встановлено!");

        java.util.Scanner scanner =
                new java.util.Scanner(System.in);

        System.out.print("Введіть телефон або email: ");
        String login = scanner.nextLine();

        System.out.print("Введіть пароль: ");
        String password = scanner.nextLine();

        try {

            login(login, password);

            System.out.println("Авторизація успішна!");
            System.out.println("Вітаємо, " + getCurrentUser());

            logout();

            System.out.println("Вихід успішний!");

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }
    }
}
