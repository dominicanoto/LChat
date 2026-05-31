package com.messenger.server;

import com.messenger.database.Database;
import com.messenger.database.DialogService;
import com.messenger.database.MessageService;
import com.messenger.database.UserService;
import com.messenger.protocol.XmlProtocol;

import java.net.BindException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final int PORT = 12345;

    private static volatile boolean running = false;

    private static ServerSocket serverSocket;

    private static ServerEventListener eventListener;

    private static final Map<String, ClientHandler>
            clients =
            new ConcurrentHashMap<>();

    private static final Set<String>
            conversations =
            ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        start();
    }

    public static synchronized void start() {

        if (running) {
            log("Server is already running");
            return;
        }

        running = true;

        Thread serverThread =
                new Thread(
                        Server::runServer,
                        "LChat server"
                );

        serverThread.start();
    }

    private static void runServer() {

        Database.initialize();

        boolean externalServerDetected = false;

        try (ServerSocket serverSocket =
                     new ServerSocket(PORT)) {

            Server.serverSocket = serverSocket;

            log(
                    "Server started on port " + PORT
            );

            status("Running on port " + PORT);

            while (running) {

                Socket socket =
                        serverSocket.accept();

                log(
                        "Client connected: " +
                                socket.getRemoteSocketAddress()
                );

                ClientHandler clientHandler =
                        new ClientHandler(socket);

                new Thread(clientHandler).start();
            }

        } catch (BindException e) {

            externalServerDetected = true;
            running = false;

            status(
                    "Already running on port " + PORT
            );

            log(
                    "Server is already running on port " +
                            PORT
            );

        } catch (IOException e) {

            if (running) {
                log(
                        "Server error: " +
                                e.getMessage()
                );
            }

        } finally {

            if (!externalServerDetected) {

                running = false;
                status("Stopped");
                log("Server stopped");
            }

        }
    }

    public static synchronized void stop() {

        running = false;

        try {

            if (serverSocket != null &&
                    !serverSocket.isClosed()) {

                serverSocket.close();
            }

        } catch (IOException e) {

            log(
                    "Server stop error: " +
                            e.getMessage()
            );
        }
    }

    public static boolean isRunning() {

        return running;
    }

    public static void setEventListener(
            ServerEventListener listener
    ) {

        eventListener = listener;
    }

    public static void addClient(
            String username,
            ClientHandler client
    ) {

        ClientHandler oldClient =
                clients.put(username, client);

        if (oldClient != null &&
                oldClient != client) {

            oldClient.close();
        }

        sendOnlineUsersTo(
                username,
                client
        );

        log(
                username + " is online"
        );

        broadcast(
                XmlProtocol.presence(
                        username,
                        "online"
                )
        );
    }

    public static void removeClient(
            String username,
            ClientHandler client
    ) {

        if (username == null) {
            return;
        }

        boolean removed =
                clients.remove(
                        username,
                        client
                );

        if (!removed) {
            return;
        }

        log(
                username + " is offline"
        );

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
                XmlProtocol.presence(
                        username,
                        "offline"
                )
        );
    }

    public static void registerConversation(
            String sender,
            String receiver,
            String message
    ) {

        String first =
                sender.compareTo(receiver) <= 0
                        ? sender
                        : receiver;

        String second =
                first.equals(sender)
                        ? receiver
                        : sender;

        String conversation =
                first + " <--> " + second;

        conversations.add(conversation);

        int dialogId =
                DialogService.getOrCreateDialog(
                        sender,
                        receiver
                );

        MessageService.saveMessage(
                dialogId,
                sender,
                receiver,
                message
        );

        log(
                sender + " -> " +
                        receiver + ": " +
                        message
        );

        if (eventListener != null) {

            eventListener.onConversation(
                    conversation
            );
        }
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

    public static void sendReadReceipt(
            String sender,
            String reader
    ) {

        MessageService.markMessagesRead(
                sender,
                reader
        );

        ClientHandler client =
                clients.get(sender);

        if (client != null) {

            client.sendMessage(
                    XmlProtocol.read(
                            reader,
                            sender
                    )
            );
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

    private static void sendOnlineUsersTo(
            String username,
            ClientHandler client
    ) {

        for (String onlineUsername :
                clients.keySet()) {

            if (!onlineUsername.equals(username)) {

                client.sendMessage(
                        XmlProtocol.presence(
                                onlineUsername,
                                "online"
                        )
                );
            }
        }
    }

    private static void log(
            String message
    ) {

        System.out.println(message);

        if (eventListener != null) {

            eventListener.onLog(message);
        }
    }

    private static void status(
            String message
    ) {

        if (eventListener != null) {

            eventListener.onStatus(message);
        }
    }

    public interface ServerEventListener {

        void onLog(String message);

        void onConversation(String conversation);

        void onStatus(String status);
    }
}
