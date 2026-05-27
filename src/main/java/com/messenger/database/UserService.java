package com.messenger.database;

import com.messenger.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    // REGISTER

    public static boolean registerUser(
            String name,
            String username,
            String password
    ) {

        String sql =
                "INSERT INTO users(name, username, password) VALUES(?, ?, ?)";

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, username);
            statement.setString(3, password);

            statement.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.out.println("User already exists");

            return false;
        }
    }

    // LOGIN

    public static boolean loginUser(
            String username,
            String password
    ) {

        String sql =
                "SELECT * FROM users WHERE username=? AND password=?";

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet =
                    statement.executeQuery();

            return resultSet.next();

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }

    // SEARCH USER

    public static User findUserByUsername(
            String username
    ) {

        String sql =
                "SELECT * FROM users WHERE username=?";

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                String name =
                        resultSet.getString("name");

                return new User(
                        name,
                        username
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return null;
    }

    public static User getUser(
            String username
    ) {

        String sql =
                "SELECT * FROM users WHERE username=?";

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                String name =
                        resultSet.getString("name");

                return new User(
                        name,
                        username
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return null;
    }
}