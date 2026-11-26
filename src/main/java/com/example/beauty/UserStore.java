
package com.example.beauty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Locale;
import java.time.LocalDateTime;

public class UserStore {

    // Зареєстровані користувачі
    private static final Map<String, User> users =
            new HashMap<>();

    // Історія візитів користувачів
    private static final Map<String, List<Payment>> paymentHistory =
            new HashMap<>();

    private static User currentUser;

    // Реєстрація користувача
    public static void register(String username,
                                String phone,
                                String email,
                                String password) {

        username = username.trim();

        phone = phone.replaceAll("[\\s()-]", "");

        email = email.trim().toLowerCase(Locale.ROOT);

        // Перевірка імені
        if (username.isBlank()) {

            throw new IllegalArgumentException(
                    "Введіть ім'я користувача!"
            );
        }

        // Перевірка номера телефону
        if (!phone.matches("\\+?[0-9]{10,15}")) {

            throw new IllegalArgumentException(
                    "Введіть коректний номер телефону!"
            );
        }

        // Нормалізація українського номера
        if (phone.matches("0[0-9]{9}")) {
            phone = "+38" + phone;
        } else if (phone.matches("380[0-9]{9}")) {
            phone = "+" + phone;
        }

        // Перевірка електронної пошти
        if (!email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            throw new IllegalArgumentException(
                    "Введіть коректну електронну пошту!"
            );
        }

        // Перевірка пароля
        if (password == null || password.length() < 4
                || password.isBlank()) {

            throw new IllegalArgumentException(
                    "Пароль має містити щонайменше 4 символи!"
            );
        }

        // Перевірка унікальності телефону та email
        for (User user : users.values()) {

            if (user.getPhone().equals(phone)) {

                throw new IllegalArgumentException(
                        "Цей номер телефону вже зареєстрований!"
                );
            }

            if (user.getEmail().equals(email)) {

                throw new IllegalArgumentException(
                        "Ця електронна пошта вже зареєстрована!"
                );
            }
        }

        // Створення нового користувача
        User user = new User(
                username,
                phone,
                email,
                password
        );

        users.put(email, user);

        paymentHistory.put(email, new ArrayList<>());
    }

    // Авторизація за телефоном або email
    public static void login(String login, String password) {

        if (login == null || login.isBlank()
                || password == null || password.isBlank()) {

            throw new IllegalArgumentException(
                    "Заповніть усі поля!"
            );
        }

        login = login.trim();

        String phone = login.replaceAll("[\\s()-]", "");

        if (phone.matches("0[0-9]{9}")) {
            phone = "+38" + phone;
        } else if (phone.matches("380[0-9]{9}")) {
            phone = "+" + phone;
        }

        for (User user : users.values()) {

            if (user.getEmail().equalsIgnoreCase(login)
                    || user.getPhone().equals(phone)) {

                if (user.getPassword().equals(password)) {

                    currentUser = user;
                    return;
                }
            }
        }

        throw new IllegalArgumentException(
                "Невірний телефон, email або пароль!"
        );
    }

    // Вихід із системи
    public static void logout() {
        currentUser = null;
    }

    // Отримання імені поточного користувача
    public static String getCurrentUser() {

        if (currentUser == null) {
            return null;
        }

        return currentUser.getUsername();
    }

    // Додавання запису на процедуру
    public static void addPayment(Payment payment) {

        if (currentUser == null) {

            throw new IllegalStateException(
                    "Спочатку авторизуйтеся!"
            );
        }

        if (payment == null
                || payment.getMaster() == null
                || payment.getDateTime() == null) {

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

        paymentHistory.computeIfAbsent(
                currentUser.getEmail(),
                k -> new ArrayList<>()
        ).add(payment);
    }

    // Отримання історії поточного користувача
    public static List<Payment> getHistory() {

        if (currentUser == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(
                paymentHistory.getOrDefault(
                        currentUser.getEmail(),
                        new ArrayList<>()
                )
        );
    }

    // Перевірка зайнятості майстра
    public static boolean isMasterBusy(
            String master, LocalDateTime dateTime) {

        for (List<Payment> history : paymentHistory.values()) {

            for (Payment payment : history) {

                if (Objects.equals(payment.getMaster(), master)
                        && Objects.equals(
                        payment.getDateTime(), dateTime)) {

                    return true;
                }
            }
        }

        return false;
    }
}
