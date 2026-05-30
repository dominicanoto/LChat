package com.messenger.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

        migrateMessagesTable();
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

                    message TEXT NOT NULL,

                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        execute(sql);
    }

    private static void migrateMessagesTable() {

        if (!columnExists("messages", "message")) {

            execute(
                    "ALTER TABLE messages " +
                            "ADD COLUMN message TEXT"
            );

            if (columnExists("messages", "text")) {

                execute(
                        "UPDATE messages " +
                                "SET message = text " +
                                "WHERE message IS NULL"
                );
            }
        }

        if (!columnExists("messages", "timestamp")) {

            execute(
                    "ALTER TABLE messages " +
                            "ADD COLUMN timestamp TEXT"
            );

            if (columnExists("messages", "created_at")) {

                execute(
                        "UPDATE messages " +
                                "SET timestamp = created_at " +
                                "WHERE timestamp IS NULL"
                );
            }

            execute(
                    "UPDATE messages " +
                            "SET timestamp = CURRENT_TIMESTAMP " +
                            "WHERE timestamp IS NULL"
            );
        }
    }

    private static boolean columnExists(
            String table,
            String column
    ) {

        String sql =
                "PRAGMA table_info(" + table + ")";

        try (
                Connection connection =
                        connect();

                Statement statement =
                        connection.createStatement();

                ResultSet resultSet =
                        statement.executeQuery(sql)
        ) {

            while (resultSet.next()) {

                if (column.equals(
                        resultSet.getString("name")
                )) {

                    return true;
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return false;
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
