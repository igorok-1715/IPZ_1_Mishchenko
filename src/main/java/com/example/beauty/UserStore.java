package com.example.beauty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Зберігає зареєстрованих користувачів і історію їхніх оплат,
 * перевіряє коректність логіна/пароля.
 */
public class UserStore {

    private static final Map<String, String> users = new HashMap<>();
    private static final Map<String, List<Payment>> paymentHistory = new HashMap<>();
    private static String currentUser;

    public static void register(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Введіть ім'я користувача.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Пароль має містити щонайменше 4 символи.");
        }
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("Користувач із таким іменем вже існує.");
        }
        users.put(username, password);
        paymentHistory.put(username, new ArrayList<>());
    }

    public static void login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Заповніть усі поля.");
        }
        String stored = users.get(username);
        if (stored == null || !stored.equals(password)) {
            throw new IllegalArgumentException("Невірне ім'я користувача або пароль.");
        }
        currentUser = username;
    }

    public static void logout() {
        currentUser = null;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static void addPayment(Payment payment) {
        paymentHistory.computeIfAbsent(currentUser, k -> new ArrayList<>()).add(payment);
    }

    public static List<Payment> getHistory() {
        return paymentHistory.getOrDefault(currentUser, new ArrayList<>());
    }
}