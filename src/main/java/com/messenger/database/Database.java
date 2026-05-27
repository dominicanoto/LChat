package com.messenger.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL =
            "jdbc:sqlite:messenger.db";

    public static Connection connect()
            throws SQLException {

        return DriverManager.getConnection(URL);

    }

    public static void initialize() {

        String usersTable = """
                CREATE TABLE IF NOT EXISTS users (

                    id INTEGER PRIMARY KEY AUTOINCREMENT,

                    name TEXT NOT NULL,

                    username TEXT UNIQUE NOT NULL,

                    password TEXT NOT NULL

                );
                """;

        String dialogsTable = """
                CREATE TABLE IF NOT EXISTS dialogs (

                    id INTEGER PRIMARY KEY AUTOINCREMENT,

                    user1 TEXT NOT NULL,

                    user2 TEXT NOT NULL

                );
                """;

        String messagesTable = """
                CREATE TABLE IF NOT EXISTS messages (

                    id INTEGER PRIMARY KEY AUTOINCREMENT,

                    dialog_id INTEGER NOT NULL,

                    sender TEXT NOT NULL,

                    receiver TEXT NOT NULL,

                    message TEXT NOT NULL,

                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP

                );
                """;

        try (Connection connection = connect();
             Statement statement =
                     connection.createStatement()) {

            statement.execute(usersTable);

            statement.execute(dialogsTable);

            statement.execute(messagesTable);

            System.out.println(
                    "Database initialized!"
            );

        } catch (SQLException e) {

            e.printStackTrace();

        }
    }
}