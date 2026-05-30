package com.messenger.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class DialogService {

    public static int findDialog(
            String user1,
            String user2
    ) {

        String selectSql = """
                SELECT id FROM dialogs
                WHERE
                (user1=? AND user2=?)
                OR
                (user1=? AND user2=?)
                """;

        try (Connection connection = Database.connect();
             PreparedStatement select =
                     connection.prepareStatement(selectSql)) {

            select.setString(1, user1);
            select.setString(2, user2);
            select.setString(3, user2);
            select.setString(4, user1);

            ResultSet resultSet =
                    select.executeQuery();

            if (resultSet.next()) {

                return resultSet.getInt("id");
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return -1;
    }

    public static int getOrCreateDialog(
            String user1,
            String user2
    ) {

        int existingDialogId =
                findDialog(
                        user1,
                        user2
                );

        if (existingDialogId != -1) {
            return existingDialogId;
        }

        String selectSql = """
                SELECT id FROM dialogs
                WHERE
                (user1=? AND user2=?)
                OR
                (user1=? AND user2=?)
                """;

        try (Connection connection = Database.connect();
             PreparedStatement select =
                     connection.prepareStatement(selectSql)) {

            select.setString(1, user1);
            select.setString(2, user2);
            select.setString(3, user2);
            select.setString(4, user1);

            ResultSet resultSet =
                    select.executeQuery();

            // Dialog exists

            if (resultSet.next()) {

                return resultSet.getInt("id");

            }

            // Create dialog

            String insertSql = """
                    INSERT INTO dialogs(user1, user2)
                    VALUES(?, ?)
                    """;

            PreparedStatement insert =
                    connection.prepareStatement(
                            insertSql,
                            Statement.RETURN_GENERATED_KEYS
                    );

            insert.setString(1, user1);
            insert.setString(2, user2);

            insert.executeUpdate();

            ResultSet generatedKeys =
                    insert.getGeneratedKeys();

            if (generatedKeys.next()) {

                return generatedKeys.getInt(1);

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return -1;
    }

    public static List<String> loadUserDialogs(
            String username
    ) {

        List<String> dialogs =
                new ArrayList<>();

        String sql = """
                SELECT * FROM dialogs
                WHERE user1=? OR user2=?
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, username);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                String user1 =
                        resultSet.getString("user1");

                String user2 =
                        resultSet.getString("user2");

                String otherUser =
                        user1.equals(username)
                                ? user2
                                : user1;

                dialogs.add(otherUser);
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return dialogs;
    }
}
