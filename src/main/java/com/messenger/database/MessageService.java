package com.messenger.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageService {

    public record MessageRecord(
            String sender,
            String message,
            String time,
            boolean read
    ) {
    }

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

    public static List<MessageRecord> loadMessageRecords(
            int dialogId
    ) {

        List<MessageRecord> messages =
                new ArrayList<>();

        String sql = """
                SELECT sender,
                       message,
                       time(timestamp) AS message_time,
                       read_at
                FROM messages
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

                String time =
                        resultSet.getString(
                                "message_time"
                        );

                messages.add(
                        new MessageRecord(
                                resultSet.getString("sender"),
                                resultSet.getString("message"),
                                time == null || time.length() < 5
                                        ? ""
                                        : time.substring(0, 5),
                                resultSet.getString("read_at") != null
                        )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return messages;
    }

    public static boolean hasUnreadMessages(
            String sender,
            String receiver
    ) {

        String sql = """
                SELECT id FROM messages
                WHERE sender=?
                AND receiver=?
                AND read_at IS NULL
                LIMIT 1
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, sender);
            statement.setString(2, receiver);

            ResultSet resultSet =
                    statement.executeQuery();

            return resultSet.next();

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    public static void markMessagesRead(
            String sender,
            String receiver
    ) {

        String sql = """
                UPDATE messages
                SET read_at = CURRENT_TIMESTAMP
                WHERE sender=?
                AND receiver=?
                AND read_at IS NULL
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, sender);
            statement.setString(2, receiver);

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }
}
