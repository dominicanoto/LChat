package com.messenger.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageService {

    public static void saveMessage(
            int dialogId,
            String sender,
            String receiver,
            String message
    ) {

        String sql = """
                INSERT INTO messages(
                    dialog_id,
                    sender,
                    receiver,
                    message
                )
                VALUES(?, ?, ?, ?)
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, dialogId);
            statement.setString(2, sender);
            statement.setString(3, receiver);
            statement.setString(4, message);

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }
    }

    public static List<String> loadMessages(
            int dialogId
    ) {

        List<String> messages =
                new ArrayList<>();

        String sql = """
                SELECT * FROM messages
                WHERE dialog_id=?
                ORDER BY timestamp
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, dialogId);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                String sender =
                        resultSet.getString("sender");

                String message =
                        resultSet.getString("message");

                messages.add(
                        sender + ": " + message
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return messages;
    }
}