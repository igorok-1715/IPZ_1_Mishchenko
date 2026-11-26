
package com.example.beauty;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Server {

    private static final int PORT = 8080;

    public static void main(String[] args) {

        try {

            Database.initialize();

            HttpServer server = HttpServer.create(
                    new InetSocketAddress("127.0.0.1", PORT), 0
            );

            server.createContext("/health", Server::handleHealth);
            server.createContext("/register", Server::handleRegister);
            server.createContext("/login", Server::handleLogin);
            server.createContext("/logout", Server::handleLogout);
            server.createContext("/appointments", Server::handleAppointment);
            server.createContext("/history", Server::handleHistory);
            server.createContext("/availability", Server::handleAvailability);

            server.start();

            System.out.println("Сервер успішно запущено!");
            System.out.println("Адреса: http://localhost:" + PORT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Перевірка працездатності сервера
    private static void handleHealth(HttpExchange exchange)
            throws IOException {

        if (!checkMethod(exchange, "GET")) {
            return;
        }

        sendResponse(exchange, 200, "Сервер працює!");
    }

    // Реєстрація користувача
    private static void handleRegister(HttpExchange exchange)
            throws IOException {

        if (!checkMethod(exchange, "POST")) {
            return;
        }

        try {

            Map<String, String> data = readData(exchange);

            UserStore.register(
                    data.getOrDefault("username", ""),
                    data.getOrDefault("phone", ""),
                    data.getOrDefault("email", ""),
                    data.getOrDefault("password", "")
            );

            sendResponse(exchange, 201,
                    "Реєстрація успішна!");

            System.out.println("Зареєстровано користувача");

        } catch (IllegalArgumentException e) {

            sendResponse(exchange, 400, e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();
            sendResponse(exchange, 500, "Помилка сервера!");
        }
    }

    // Авторизація користувача
    private static void handleLogin(HttpExchange exchange)
            throws IOException {

        if (!checkMethod(exchange, "POST")) {
            return;
        }

        try {

            Map<String, String> data = readData(exchange);

            User user = UserStore.authenticate(
                    data.get("login"),
                    data.get("password")
            );

            String token = SessionManager.createSession(user);

            sendResponse(exchange, 200,
                    token + "\n" + user.getUsername());

            System.out.println("Користувач авторизувався");

        } catch (IllegalArgumentException e) {

            sendResponse(exchange, 401, e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();
            sendResponse(exchange, 500, "Помилка сервера!");
        }
    }

    // Вихід користувача із системи
    private static void handleLogout(HttpExchange exchange)
            throws IOException {

        if (!checkMethod(exchange, "POST")) {
            return;
        }

        String token = exchange.getRequestHeaders()
                .getFirst("Authorization");

        if (token == null ||
                !SessionManager.isAuthorized(token)) {

            sendResponse(exchange, 401,
                    "Необхідно авторизуватися!");

            return;
        }

        SessionManager.removeSession(token);

        sendResponse(exchange, 200,
                "Вихід успішний!");

        System.out.println("Користувач вийшов із системи");
    }

    // Отримання авторизованого користувача
    private static User getAuthorizedUser(HttpExchange exchange) {

        String token = exchange.getRequestHeaders()
                .getFirst("Authorization");

        if (token == null || token.isBlank()) {
            return null;
        }

        return SessionManager.getUser(token);
    }

    // Запис на процедуру
    private static void handleAppointment(HttpExchange exchange)
            throws IOException {

        if (!checkMethod(exchange, "POST")) {
            return;
        }

        User user = getAuthorizedUser(exchange);

        if (user == null) {

            sendResponse(exchange, 401,
                    "Спочатку авторизуйтеся!");

            return;
        }

        try {

            Map<String, String> data = readData(exchange);

            String service = data.get("service");
            String master = data.get("master");

            LocalDateTime dateTime = LocalDateTime.parse(
                    data.getOrDefault("dateTime", "")
            );

            // Перевірка дати та робочого часу
            validateVisitTime(dateTime);

            // Вартість визначається сервером
            double amount = getServicePrice(service, master);

            Payment payment = new Payment(
                    service,
                    master,
                    data.getOrDefault("details", ""),
                    amount,
                    dateTime
            );

            UserStore.addPayment(user, payment);

            sendResponse(exchange, 201,
                    "Запис успішно створено!");

            System.out.println(
                    "Створено новий запис на процедуру"
            );

        } catch (IllegalArgumentException |
                 java.time.DateTimeException e) {

            sendResponse(exchange, 400, e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, 500,
                    "Помилка сервера!");
        }
    }

    // Отримання історії візитів
    private static void handleHistory(HttpExchange exchange)
            throws IOException {

        if (!checkMethod(exchange, "GET")) {
            return;
        }

        User user = getAuthorizedUser(exchange);

        if (user == null) {

            sendResponse(exchange, 401,
                    "Спочатку авторизуйтеся!");

            return;
        }

        try {

            List<Payment> history =
                    UserStore.getHistory(user);

            StringBuilder response = new StringBuilder();

            for (Payment payment : history) {

                response.append(encode(payment.getType()))
                        .append("|")
                        .append(encode(payment.getMaster()))
                        .append("|")
                        .append(encode(payment.getDetails()))
                        .append("|")
                        .append(payment.getAmount())
                        .append("|")
                        .append(payment.getDateTime())
                        .append("\n");
            }

            sendResponse(exchange, 200,
                    response.toString());

            System.out.println(
                    "Надіслано історію візитів"
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, 500,
                    "Не вдалося отримати історію!");
        }
    }

    // Перевірка зайнятості майстра
    private static void handleAvailability(HttpExchange exchange)
            throws IOException {

        if (!checkMethod(exchange, "POST")) {
            return;
        }

        if (getAuthorizedUser(exchange) == null) {

            sendResponse(exchange, 401,
                    "Спочатку авторизуйтеся!");

            return;
        }

        try {

            Map<String, String> data = readData(exchange);

            String master = data.get("master");

            LocalDateTime dateTime = LocalDateTime.parse(
                    data.getOrDefault("dateTime", "")
            );

            if (master == null || master.isBlank()) {

                throw new IllegalArgumentException(
                        "Не обрано майстра!"
                );
            }

            boolean busy = UserStore.isMasterBusy(
                    master, dateTime
            );

            sendResponse(exchange, 200,
                    String.valueOf(busy));

        } catch (IllegalArgumentException |
                 java.time.DateTimeException e) {

            sendResponse(exchange, 400,
                    "Некоректні дані!");

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, 500,
                    "Помилка перевірки зайнятості!");
        }
    }

    // Перевірка дати та часу візиту
    private static void validateVisitTime(
            LocalDateTime dateTime) {

        if (!dateTime.isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Не можна записатися на минулий час!"
            );
        }

        LocalTime time = dateTime.toLocalTime();

        if (time.isBefore(LocalTime.of(9, 0))
                || time.isAfter(LocalTime.of(18, 0))
                || time.getMinute() != 0
                || time.getSecond() != 0
                || time.getNano() != 0) {

            throw new IllegalArgumentException(
                    "Оберіть час з 09:00 до 18:00 з кроком в одну годину!"
            );
        }
    }

    // Визначення вартості процедури
    private static double getServicePrice(
            String service, String master) {

        if (service == null || master == null) {

            throw new IllegalArgumentException(
                    "Оберіть послугу та майстра!"
            );
        }

        return switch (service + "|" + master) {

            case "Манікюр|Олена" -> 500.0;
            case "Манікюр|Ірина" -> 600.0;

            case "Стрижка|Андрій" -> 700.0;
            case "Стрижка|Максим" -> 900.0;

            case "Масаж обличчя|Катерина" -> 800.0;

            case "Фарбування волосся|Світлана" -> 1200.0;
            case "Фарбування волосся|Андрій" -> 1400.0;

            case "SPA-процедура|Олена" -> 1500.0;
            case "SPA-процедура|Катерина" -> 1800.0;

            default -> throw new IllegalArgumentException(
                    "Некоректна послуга або майстер!"
            );
        };
    }

    // Кодування текстових даних
    private static String encode(String value) {

        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }

    // Перевірка HTTP-методу
    private static boolean checkMethod(
            HttpExchange exchange, String method)
            throws IOException {

        if (!exchange.getRequestMethod().equals(method)) {

            sendResponse(exchange, 405,
                    "Метод не підтримується!");

            return false;
        }

        return true;
    }

    // Зчитування даних HTTP-запиту
    private static Map<String, String> readData(
            HttpExchange exchange) throws IOException {

        String body;

        try (var input = exchange.getRequestBody()) {

            byte[] bytes = input.readNBytes(16385);

            if (bytes.length > 16384) {

                throw new IllegalArgumentException(
                        "Запит занадто великий!"
                );
            }

            body = new String(
                    bytes, StandardCharsets.UTF_8
            );
        }

        Map<String, String> data = new HashMap<>();

        for (String pair : body.split("&")) {

            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {

                String key = URLDecoder.decode(
                        parts[0], StandardCharsets.UTF_8
                );

                String value = URLDecoder.decode(
                        parts[1], StandardCharsets.UTF_8
                );

                data.put(key, value);
            }
        }

        return data;
    }

    // Надсилання відповіді клієнту
    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response) throws IOException {

        byte[] bytes = response.getBytes(
                StandardCharsets.UTF_8
        );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/plain; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                statusCode,
                bytes.length == 0 ? -1 : bytes.length
        );

        try (OutputStream output =
                     exchange.getResponseBody()) {

            if (bytes.length > 0) {
                output.write(bytes);
            }

        } finally {
            exchange.close();
        }
    }
}
