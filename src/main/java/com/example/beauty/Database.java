
package com.example.beauty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    // Шлях до бази даних
    private static final String URL =
            "jdbc:sqlite:beauty_salon.db";

    // Підключення до бази даних
    public static Connection connect() throws SQLException {

        Connection connection = DriverManager.getConnection(URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }

        return connection;
    }

    // Створення таблиць
    public static void initialize() {

        String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    phone TEXT NOT NULL UNIQUE,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL
                )
                """;

        String appointmentsTable = """
                CREATE TABLE IF NOT EXISTS appointments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    service TEXT NOT NULL,
                    master TEXT NOT NULL,
                    visit_datetime TEXT NOT NULL,
                    details TEXT,
                    amount REAL NOT NULL CHECK (amount > 0),
                    UNIQUE (master, visit_datetime),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """;

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {

            statement.execute(usersTable);
            statement.execute(appointmentsTable);

            System.out.println("База даних успішно підключена!");
            System.out.println("Таблиці успішно створені!");

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Помилка створення бази даних", e
            );
        }
    }
}
