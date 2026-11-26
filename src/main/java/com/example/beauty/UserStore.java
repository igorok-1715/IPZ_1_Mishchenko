
package com.example.beauty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.LocalDateTime;

public class UserStore {

    private static User currentUser;

    // Реєстрація користувача
    public static void register(String username,
                                String phone,
                                String email,
                                String password) {

        if (username == null || phone == null
                || email == null || password == null) {

            throw new IllegalArgumentException(
                    "Заповніть усі поля!"
            );
        }

        username = username.trim();
        phone = normalizePhone(phone);
        email = email.trim().toLowerCase(Locale.ROOT);

        // Перевірка імені
        if (username.isBlank()) {
            throw new IllegalArgumentException(
                    "Введіть ім'я користувача!"
            );
        }

        // Перевірка телефону
        if (!phone.matches("\\+?[0-9]{10,15}")) {
            throw new IllegalArgumentException(
                    "Введіть коректний номер телефону!"
            );
        }

        // Перевірка email
        if (!email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            throw new IllegalArgumentException(
                    "Введіть коректну електронну пошту!"
            );
        }

        // Перевірка пароля
        if (password.length() < 4 || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Пароль має містити щонайменше 4 символи!"
            );
        }

        String checkSql = """
                SELECT phone, email FROM users
                WHERE phone = ? OR email = ?
                """;

        String insertSql = """
                INSERT INTO users
                (username, phone, email, password_hash)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = Database.connect()) {

            // Перевірка унікальності контактів
            try (PreparedStatement statement =
                         connection.prepareStatement(checkSql)) {

                statement.setString(1, phone);
                statement.setString(2, email);

                try (ResultSet result = statement.executeQuery()) {

                    while (result.next()) {

                        if (phone.equals(result.getString("phone"))) {
                            throw new IllegalArgumentException(
                                    "Цей номер телефону вже зареєстрований!"
                            );
                        }

                        if (email.equals(result.getString("email"))) {
                            throw new IllegalArgumentException(
                                    "Ця електронна пошта вже зареєстрована!"
                            );
                        }
                    }
                }
            }

            // Збереження користувача в базі даних
            try (PreparedStatement statement =
                         connection.prepareStatement(insertSql)) {

                statement.setString(1, username);
                statement.setString(2, phone);
                statement.setString(3, email);

                statement.setString(
                        4, PasswordUtils.hashPassword(password)
                );

                statement.executeUpdate();
            }

        } catch (SQLException e) {

            e.printStackTrace();

            throw new IllegalArgumentException(
                    "Не вдалося зареєструвати користувача."
            );
        }
    }

    // Авторизація користувача
    public static void login(String login, String password) {

        currentUser = null;

        if (login == null || login.isBlank()
                || password == null || password.isBlank()) {

            throw new IllegalArgumentException(
                    "Заповніть усі поля!"
            );
        }

        String email = login.trim().toLowerCase(Locale.ROOT);
        String phone = normalizePhone(login);

        String sql = """
                SELECT * FROM users
                WHERE email = ? OR phone = ?
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setString(2, phone);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    String passwordHash =
                            result.getString("password_hash");

                    if (PasswordUtils.verifyPassword(
                            password, passwordHash)) {

                        currentUser = new User(
                                result.getInt("id"),
                                result.getString("username"),
                                result.getString("phone"),
                                result.getString("email")
                        );

                        return;
                    }
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            throw new IllegalArgumentException(
                    "Помилка підключення до бази даних!"
            );
        }

        throw new IllegalArgumentException(
                "Невірний телефон, email або пароль!"
        );
    }

    // Вихід із системи
    public static void logout() {
        currentUser = null;
    }

    // Отримання імені користувача
    public static String getCurrentUser() {

        if (currentUser == null) {
            return null;
        }

        return currentUser.getUsername();
    }

    // Збереження запису на процедуру
    public static void addPayment(Payment payment) {

        if (currentUser == null) {
            throw new IllegalStateException(
                    "Спочатку авторизуйтеся!"
            );
        }

        if (payment == null
                || payment.getType() == null
                || payment.getMaster() == null
                || payment.getDateTime() == null
                || !Double.isFinite(payment.getAmount())
                || payment.getAmount() <= 0) {

            throw new IllegalArgumentException(
                    "Некоректні дані запису!"
            );
        }

        if (!payment.getDateTime().isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Не можна записатися на минулий час!"
            );
        }

        if (isMasterBusy(
                payment.getMaster(),
                payment.getDateTime())) {

            throw new IllegalArgumentException(
                    "Майстер уже зайнятий на цей час!"
            );
        }

        String sql = """
                INSERT INTO appointments
                (user_id, service, master,
                 visit_datetime, details, amount)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, currentUser.getId());
            statement.setString(2, payment.getType());
            statement.setString(3, payment.getMaster());

            statement.setString(
                    4, payment.getDateTime().toString()
            );

            statement.setString(5, payment.getDetails());
            statement.setDouble(6, payment.getAmount());

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

            if (e.getMessage() != null
                    && e.getMessage().contains(
                    "UNIQUE constraint failed")) {

                throw new IllegalArgumentException(
                        "Майстер уже зайнятий на цей час!"
                );
            }

            throw new IllegalArgumentException(
                    "Не вдалося зберегти запис!"
            );
        }
    }

    // Отримання історії візитів
    public static List<Payment> getHistory() {

        List<Payment> history = new ArrayList<>();

        if (currentUser == null) {
            return history;
        }

        String sql = """
                SELECT * FROM appointments
                WHERE user_id = ?
                ORDER BY visit_datetime DESC
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, currentUser.getId());

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Payment payment = new Payment(
                            result.getString("service"),
                            result.getString("master"),
                            result.getString("details"),
                            result.getDouble("amount"),
                            LocalDateTime.parse(
                                    result.getString("visit_datetime")
                            )
                    );

                    history.add(payment);
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            throw new IllegalStateException(
                    "Не вдалося завантажити історію!", e
            );
        }

        return history;
    }

    // Перевірка зайнятості майстра
    public static boolean isMasterBusy(
            String master, LocalDateTime dateTime) {

        if (master == null || dateTime == null) {
            return false;
        }

        String sql = """
                SELECT id FROM appointments
                WHERE master = ? AND visit_datetime = ?
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, master);
            statement.setString(2, dateTime.toString());

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {

            e.printStackTrace();

            throw new IllegalStateException(
                    "Помилка перевірки зайнятості майстра!", e
            );
        }
    }

    // Нормалізація номера телефону
    private static String normalizePhone(String phone) {

        phone = phone.replaceAll("[\\s()-]", "");

        if (phone.matches("0[0-9]{9}")) {
            phone = "+38" + phone;

        } else if (phone.matches("380[0-9]{9}")) {
            phone = "+" + phone;
        }

        return phone;
    }
}
