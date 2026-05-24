package com.messenger.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final int PORT = 1234;

    private static final List<ClientHandler> clients =
            new ArrayList<>();

    public static void main(String[] args) {

        System.out.println("Server started...");

        try (ServerSocket serverSocket =
                     new ServerSocket(PORT)) {

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println(
                        "New client connected"
                );

                ClientHandler client =
                        new ClientHandler(socket);

                clients.add(client);

                client.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message) {

        for (ClientHandler client : clients) {

            client.sendMessage(message);

        }
    }
}