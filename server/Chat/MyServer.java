package server.Chat;

import server.auth.BaseAuthService;
import server.handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private final ServerSocket serverSocket;
    private final BaseAuthService authService;
    private final List<ClientHandler> clients = new ArrayList<>();
    private static final String CLIENT_CONNECT = "Подключился";
    private static final String CLIENT_DISCONNECT = "Отключился";

    public MyServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.authService = new BaseAuthService();
    }

    public BaseAuthService getAuthService() {
        return authService;
    }

    public void start() {
        System.out.println("Сервер запущен");

        try {
            while (true) {
                waitAndProcessClientConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void waitAndProcessClientConnection() throws IOException {
        System.out.println("Ожидание пользователя");
        Socket socket = serverSocket.accept();
        System.out.println("Клиент подключился");

        processClientConnection(socket);
    }

    private void processClientConnection(Socket socket) throws IOException {
        ClientHandler clientHandler = new ClientHandler(this, socket);
        clientHandler.handle();
    }

    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        System.out.println(clientHandler.getUserName() + " " + CLIENT_CONNECT);
        clients.add(clientHandler);
        serverMessage(clientHandler, CLIENT_CONNECT);
    }



    public synchronized void unsubscribe(ClientHandler clientHandler) throws IOException {
        System.out.println(clientHandler.getUserName() + " " + CLIENT_DISCONNECT);
        clients.remove(clientHandler);
        serverMessage(clientHandler, CLIENT_DISCONNECT);
    }

    public synchronized boolean isUserNameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUserName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private void serverMessage(ClientHandler clientHandler, String clientStatus) throws IOException {
        for (ClientHandler client : clients) {
            if (client == clientHandler) {
                continue;
            }
            client.sendServerMessage(clientHandler.getUserName(), clientStatus);
        }
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender) throws IOException {
        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            client.sendClientMessage(sender.getUserName(), message);

        }
    }

    public void privateMessage(String message, ClientHandler sender, String recipient) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getUserName().equals(recipient)) {
                client.sendPrivatMessage(sender.getUserName(), message);
            }
        }
    }
}
