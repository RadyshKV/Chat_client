package GB.server.Chat;

import GB.server.auth.DBAuthService;
import GB.server.handler.ClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private final ServerSocket serverSocket;
    private final DBAuthService authService;
    private final List<ClientHandler> clients = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger("ServerLogger");

    public MyServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.authService = new DBAuthService();
    }

    public DBAuthService getAuthService() {
        return authService;
    }

    public void start() {
        logger.info("Сервер запущен");

        try {
            while (true) {
                waitAndProcessClientConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void waitAndProcessClientConnection() throws IOException {
        logger.info("Ожидание пользователя");
        Socket socket = serverSocket.accept();
        logger.info("Клиент подключился");

        processClientConnection(socket);
    }

    private void processClientConnection(Socket socket) throws IOException {
        ClientHandler clientHandler = new ClientHandler(this, socket);
        clientHandler.handle();
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public synchronized boolean isUserNameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastUserListMessage() {
        StringBuilder message = new StringBuilder();
        for (ClientHandler client : clients) {
            message.append(client.getUsername() + " ");
        }
        for (ClientHandler client : clients) {
            client.sendUserListMessage(message.toString());
        }
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender, boolean isServerMessage) {
        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            client.sendMessage(sender.getUsername(), message, isServerMessage);

        }
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender) {
        broadcastMessage(message, sender, false);
    }

    public synchronized void privateMessage(String message, ClientHandler sender, String recipient) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(recipient)) {
                client.sendMessage(sender.getUsername(), message, false);
            }
        }
    }


}
