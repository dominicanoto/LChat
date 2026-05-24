package com.messenger.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageService {

    public static void saveMessage(
            String username,
            String message
    ) {

        String sql =
                "INSERT INTO messages(username, message) VALUES(?, ?)";

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, message);

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }
    }

    public static List<String> loadMessages() {

        List<String> messages =
                new ArrayList<>();

        String sql =
                "SELECT * FROM messages";

        try (Connection connection = Database.connect();
             Statement statement =
                     connection.createStatement();
             ResultSet resultSet =
                     statement.executeQuery(sql)) {

            while (resultSet.next()) {

                String username =
                        resultSet.getString("username");

                String message =
                        resultSet.getString("message");

                messages.add(
                        username + ": " + message
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return messages;
    }
}