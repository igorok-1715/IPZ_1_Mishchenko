
package com.example.beauty;

import java.net.URI;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

        if (parts.length != 2 || parts[0].isBlank()) {

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

        try {

            if (token != null) {
                sendPost("/logout", "");
            }

        } finally {

            token = null;
            currentUsername = null;
        }
    }

    // Перевірка з'єднання із сервером
    public static boolean checkConnection() {

        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8
                    )
            );

            return response.statusCode() == 200;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            return false;

        } catch (Exception e) {

            return false;
        }
    }

    // Запис на процедуру через сервер
    public static void addPayment(Payment payment) {

        checkAuthorization();

        if (payment == null
                || payment.getDateTime() == null) {

            throw new IllegalArgumentException(
                    "Некоректні дані запису!"
            );
        }

        String body =
                "service=" + encode(payment.getType())
                        + "&master=" + encode(payment.getMaster())
                        + "&details=" + encode(payment.getDetails())
                        + "&dateTime=" + encode(
                        payment.getDateTime().toString()
                );

        sendPost("/appointments", body);
    }

    // Перевірка зайнятості майстра
    public static boolean isMasterBusy(
            String master, LocalDateTime dateTime) {

        checkAuthorization();

        if (master == null || dateTime == null) {

            throw new IllegalArgumentException(
                    "Оберіть майстра, дату та час!"
            );
        }

        String body =
                "master=" + encode(master)
                        + "&dateTime=" + encode(dateTime.toString());

        String response = sendPost(
                "/availability", body
        ).trim();

        if (!response.equals("true")
                && !response.equals("false")) {

            throw new IllegalStateException(
                    "Некоректна відповідь сервера!"
            );
        }

        return Boolean.parseBoolean(response);
    }

    // Отримання історії візитів із сервера
    public static List<Payment> getHistory() {

        checkAuthorization();

        List<Payment> history = new ArrayList<>();

        String response = sendGet("/history");

        if (response.isBlank()) {
            return history;
        }

        for (String line : response.split("\n")) {

            if (line.isBlank()) {
                continue;
            }

            String[] parts = line.split("\\|", -1);

            if (parts.length != 5) {

                throw new IllegalStateException(
                        "Некоректні дані історії!"
                );
            }

            Payment payment = new Payment(
                    decode(parts[0]),
                    decode(parts[1]),
                    decode(parts[2]),
                    Double.parseDouble(parts[3]),
                    LocalDateTime.parse(parts[4])
            );

            history.add(payment);
        }

        return history;
    }

    // Перевірка авторизації
    private static void checkAuthorization() {

        if (token == null || token.isBlank()) {

            throw new IllegalStateException(
                    "Спочатку авторизуйтеся!"
            );
        }
    }

    // Передавання POST-запиту
    private static String sendPost(String path, String body) {

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create(SERVER_URL + path))
                        .timeout(Duration.ofSeconds(10))
                        .header(
                                "Content-Type",
                                "application/x-www-form-urlencoded; charset=UTF-8"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        body,
                                        StandardCharsets.UTF_8
                                )
                        );

        if (token != null) {
            builder.header("Authorization", token);
        }

        return sendRequest(builder.build());
    }

    // Передавання GET-запиту
    private static String sendGet(String path) {

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create(SERVER_URL + path))
                        .timeout(Duration.ofSeconds(10))
                        .GET();

        if (token != null) {
            builder.header("Authorization", token);
        }

        return sendRequest(builder.build());
    }

    // Надсилання HTTP-запиту та отримання відповіді
    private static String sendRequest(HttpRequest request) {

        try {

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8
                    )
            );

            int status = response.statusCode();

            if (status >= 500) {

                throw new IllegalStateException(
                        response.body()
                );
            }

            if (status < 200 || status >= 300) {

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

    // Кодування даних
    private static String encode(String value) {

        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }

    // Декодування даних
    private static String decode(String value) {

        return URLDecoder.decode(
                value,
                StandardCharsets.UTF_8
        );
    }

    // Тестування клієнт-серверної взаємодії
    public static void main(String[] args) {

        if (!checkConnection()) {

            System.out.println("Сервер недоступний!");
            return;
        }

        System.out.println("З'єднання встановлено!");

        Scanner scanner = new Scanner(System.in);

        System.out.print("Введіть телефон або email: ");
        String login = scanner.nextLine();

        System.out.print("Введіть пароль: ");
        String password = scanner.nextLine();

        try {

            login(login, password);

            System.out.println("Авторизація успішна!");
            System.out.println("Вітаємо, " + getCurrentUser());

            // Отримання історії візитів
            System.out.println("\nІсторія візитів:");

            List<Payment> history = getHistory();

            if (history.isEmpty()) {

                System.out.println("Історія візитів порожня.");

            } else {

                for (Payment payment : history) {

                    System.out.println(
                            payment.getType() + " | "
                                    + payment.getMaster() + " | "
                                    + payment.getFormattedDateTime() + " | "
                                    + payment.getDetails() + " | "
                                    + payment.getAmount() + " грн"
                    );
                }
            }

            // Перевірка зайнятості майстра
            System.out.println("\nПеревірка зайнятості:");

            System.out.print("Введіть ім'я майстра: ");
            String master = scanner.nextLine();

            System.out.print(
                    "Введіть дату (yyyy-MM-dd): "
            );

            String date = scanner.nextLine();

            System.out.print("Введіть час (HH:mm): ");
            String time = scanner.nextLine();

            LocalDateTime dateTime = LocalDateTime.parse(
                    date + "T" + time
            );

            boolean busy = isMasterBusy(master, dateTime);

            if (busy) {

                System.out.println(
                        "Майстер зайнятий на цей час!"
                );

            } else {

                System.out.println(
                        "Майстер не має запису на цей час!"
                );
            }

        } catch (Exception e) {

            System.out.println("Помилка: " + e.getMessage());

        } finally {

            if (token != null) {

                try {

                    logout();

                    System.out.println("\nВихід успішний!");

                } catch (Exception e) {

                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
