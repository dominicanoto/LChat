package com.messenger.database;

import com.messenger.model.User;

import java.sql.*;

public class UserService {

    public static boolean registerUser(
            String name,
            String username,
            String password
    ) {

        String sql =
                "INSERT INTO users(name, username, password) VALUES(?, ?, ?)";

        try (
                Connection connection =
                        Database.connect();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, name);

            statement.setString(2, username);

            statement.setString(3, password);

            statement.executeUpdate();

            return true;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }

    public static boolean loginUser(
            String username,
            String password
    ) {

        String sql =
                "SELECT * FROM users WHERE username = ? AND password = ?";

        try (
                Connection connection =
                        Database.connect();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

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

    public static User findUserByUsername(
            String username
    ) {

        String sql =
                "SELECT * FROM users WHERE username = ?";

        try (
                Connection connection =
                        Database.connect();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                return new User(
                        resultSet.getString("name"),
                        resultSet.getString("username")
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

        return findUserByUsername(username);
    }

    public static boolean updateName(
            String username,
            String name
    ) {

        String sql =
                "UPDATE users SET name = ? WHERE username = ?";

        try (
                Connection connection =
                        Database.connect();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, name);

            statement.setString(2, username);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    public static void updateLastSeen(
            String username,
            String lastSeen
    ) {

        String sql =
                "UPDATE users SET last_seen = ? WHERE username = ?";

        try (
                Connection connection =
                        Database.connect();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, lastSeen);

            statement.setString(2, username);

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public static String getLastSeen(
            String username
    ) {

        String sql =
                "SELECT last_seen FROM users WHERE username = ?";

        try (
                Connection connection =
                        Database.connect();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                return resultSet.getString(
                        "last_seen"
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return "offline";
    }
}
