package com.messenger.server;

import com.messenger.database.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final int PORT = 12345;

    private static final Map<String, ClientHandler>
            clients =
            new ConcurrentHashMap<>();

    public static void main(String[] args) {

        System.out.println(
                "Server started..."
        );

        try (ServerSocket serverSocket =
                     new ServerSocket(PORT)) {

            while (true) {

                Socket socket =
                        serverSocket.accept();

                System.out.println(
                        "Client connected"
                );

                ClientHandler clientHandler =
                        new ClientHandler(socket);

                new Thread(clientHandler).start();
            }

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    public static void addClient(
            String username,
            ClientHandler client
    ) {

        clients.put(username, client);

        broadcast(
                "SYSTEM_ONLINE:" + username
        );
    }

    public static void removeClient(
            String username
    ) {

        clients.remove(username);

        String time =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter
                                        .ofPattern(
                                                "HH:mm"
                                        )
                        );

        UserService.updateLastSeen(
                username,
                "last seen at " + time
        );

        broadcast(
                "SYSTEM_OFFLINE:" + username
        );
    }

    public static void sendToUser(
            String username,
            String message
    ) {

        ClientHandler client =
                clients.get(username);

        if (client != null) {

            client.sendMessage(message);

        }
    }

    public static void broadcast(
            String message
    ) {

        for (ClientHandler client :
                clients.values()) {

            client.sendMessage(message);
        }
    }
}