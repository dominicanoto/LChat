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

        createUsersTable();

        createDialogsTable();

        createMessagesTable();
    }

    private static void createUsersTable() {

        String sql =
                """
                CREATE TABLE IF NOT EXISTS users (

                    id INTEGER PRIMARY KEY AUTOINCREMENT,

                    name TEXT NOT NULL,

                    username TEXT UNIQUE NOT NULL,

                    password TEXT NOT NULL,

                    last_seen TEXT
                )
                """;

        execute(sql);
    }

    private static void createDialogsTable() {

        String sql =
                """
                CREATE TABLE IF NOT EXISTS dialogs (

                    id INTEGER PRIMARY KEY AUTOINCREMENT,

                    user1 TEXT NOT NULL,

                    user2 TEXT NOT NULL
                )
                """;

        execute(sql);
    }

    private static void createMessagesTable() {

        String sql =
                """
                CREATE TABLE IF NOT EXISTS messages (

                    id INTEGER PRIMARY KEY AUTOINCREMENT,

                    dialog_id INTEGER NOT NULL,

                    sender TEXT NOT NULL,

                    receiver TEXT NOT NULL,

                    text TEXT NOT NULL,

                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        execute(sql);
    }

    private static void execute(
            String sql
    ) {

        try (
                Connection connection =
                        connect();

                Statement statement =
                        connection.createStatement()
        ) {

            statement.execute(sql);

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }
}