package client.models;

import client.controllers.ChatController;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Network {

    private static final String AUTH_CMD_PREFIX = "/auth";
    private static final String AUTHOK_CMD_PREFIX = "/authok";
    private static final String AUTHERR_CMD_PREFIX = "/autherr";
    private static final String CLIENT_MSG_CMD_PREFIX = "/clientMsg";
    private static final String SERVER_MSG_CMD_PREFIX = "/serverMsg";
    private static final String PRIVATE_MSG_CMD_PREFIX = "/w";
    private static final String END_CMD_PREFIX = "/end";
    private static final String USERS_LIST_PREFIX = "/users"; //

    private static final int DEFAULT_SERVER_SOCKET = 8888;
    private static final String DEFAULT_SERVER_HOST = "localhost";

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final int port;
    private final String host;
    private String username;

    public Network(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public Network() {
            this.host = DEFAULT_SERVER_HOST;
            this.port = DEFAULT_SERVER_SOCKET;
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Соединение не установлено");
            e.printStackTrace();
        }
    }

    public DataOutputStream getOut() {
        return out;
    }


    public void waitMessage(ChatController chatController) {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith(CLIENT_MSG_CMD_PREFIX)){
                        String[] parts = message.split("\\s+", 3);
                        String sender = parts[1];
                        String messageFromUser = parts[2];
                        Platform.runLater( ()-> chatController.appendMessage(String.format("%s: %s", sender, messageFromUser)));
                    }
                    else if (message.startsWith(SERVER_MSG_CMD_PREFIX)){
                        String[] parts = message.split("\\s+", 3);
                        String messageFromUser = parts[2];
                        Platform.runLater( ()-> chatController.appendMessage(messageFromUser));
                    }
                    else if (message.startsWith(USERS_LIST_PREFIX)){
                        String[] parts = message.split("\\s+", 3);
                        String[] userList = parts[2].split("\\s+");
                        Platform.runLater( ()-> chatController.setUsersList(userList));
                    }
                    else {
                        Platform.runLater( ()-> System.out.println("Неизвестная ошибка сервера"));
                    }
                }
            } catch (IOException e) {
                System.out.println("Ошибка подключения");
            }

        });

        thread.setDaemon(true);
        thread.start();
    }

    public String sendAuthCommand(String login, String password) {
        try {
            out.writeUTF(String.format("%s %s %s", AUTH_CMD_PREFIX, login, password));
            String response = in.readUTF();
            if (response.startsWith(AUTHOK_CMD_PREFIX)){
                this.username = response.split("\\s+", 2)[1];
                return null;
            } else {
                return response.split("\\s+", 2)[1];
            }
        } catch (EOFException e) {
            return "Ошибка получения сообщения";
        } catch (IOException e) {
            return e.getMessage();
        }

    }

    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
    }

    public void sendPrivateMessage(String message, String recepient) throws IOException {
        String command = String.format("%s %s %s", PRIVATE_MSG_CMD_PREFIX, recepient, message);
        sendMessage(command);
    }
}
