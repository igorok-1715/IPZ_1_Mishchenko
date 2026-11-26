
package com.example.beauty;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.time.Duration;

public class SessionManager {

    private record Session(User user, Instant expiresAt) {}

    private static final Map<String, Session> sessions =
            new ConcurrentHashMap<>();

    // Створення сесії користувача
    public static String createSession(User user) {

        String token = UUID.randomUUID().toString();

        Session session = new Session(
                user,
                Instant.now().plus(Duration.ofHours(8))
        );

        sessions.put(token, session);

        return token;
    }

    // Отримання користувача за токеном
    public static User getUser(String token) {

        if (token == null) {
            return null;
        }

        Session session = sessions.get(token);

        if (session == null) {
            return null;
        }

        if (!session.expiresAt().isAfter(Instant.now())) {

            sessions.remove(token);
            return null;
        }

        return session.user();
    }

    // Перевірка авторизації
    public static boolean isAuthorized(String token) {

        return getUser(token) != null;
    }

    // Завершення сесії
    public static void removeSession(String token) {

        if (token != null) {
            sessions.remove(token);
        }
    }
}
