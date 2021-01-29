package server.handler;

import server.Chat.MyServer;
import server.auth.AuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + pass
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/clientMsg"; // + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/serverMsg"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/w"; // sender/recipient + msg
    private static final String END_CMD_PREFIX = "/end"; //

    private final MyServer myServer;
    private final Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private String userName;


    public ClientHandler(MyServer myServer, Socket socket) {
        this.myServer = myServer;
        this.clientSocket = socket;

    }

    public void handle() throws IOException {
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());

        new Thread(() -> {
            try {
                authentication();
                readMessage();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }


        }).start();
    }

    private void readMessage() throws IOException {
        while (true) {
            String message = in.readUTF();
            System.out.println("message| " + userName + ": " + message);
            if (message.startsWith(END_CMD_PREFIX)) {
                return;
            } else if (message.startsWith(PRIVATE_MSG_CMD_PREFIX)) {
                prepareAndForwardPrivatMessage(message.replace(PRIVATE_MSG_CMD_PREFIX, ""));
            } else if (message.startsWith(CLIENT_MSG_CMD_PREFIX)) {
                myServer.broadcastMessage(message.replace(CLIENT_MSG_CMD_PREFIX, "").trim(), this);
            } else {
                System.out.println("Получено некорректное сообщение");
            }
        }

    }

    private void prepareAndForwardPrivatMessage(String message) throws IOException {
        String[] parts = message.trim().split("\\s+", 2);
        String recipient = parts[0];
        String preparedMessage = parts[1];
        myServer.privateMessage(preparedMessage, this, recipient);
    }

    private void authentication() throws IOException {
        while (true) {
            String message = in.readUTF();
            if (message.startsWith(AUTH_CMD_PREFIX)) {
                boolean isSuccessAuth = processAuthCommand(message);
                if (isSuccessAuth) {
                    break;
                }
            } else {
                out.writeUTF(AUTHERR_CMD_PREFIX + " Ошибка авторизации");
            }
        }

    }

    private boolean processAuthCommand(String message) throws IOException {
        String[] parts = message.split("\\s+", 3);
        String login = parts[1];
        String password = parts[2];

        AuthService authService = myServer.getAuthService();
        userName = authService.getUserNameByLoginAndPassword(login, password);
        if (userName != null) {
            if (myServer.isUserNameBusy(userName)) {
                out.writeUTF(AUTHERR_CMD_PREFIX + " Логин занят");
                return false;
            }
            out.writeUTF(AUTHOK_CMD_PREFIX + " " + userName);
            myServer.subscribe(this);
            return true;
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Логин или пароль неверны");
            return false;
        }

    }

    public String getUserName() {
        return userName;
    }

    public void sendClientMessage(String username, String message) throws IOException {
        sendMessage(username, message, CLIENT_MSG_CMD_PREFIX);
    }

    public void sendPrivatMessage(String username, String message) throws IOException {
        sendMessage(username, message, PRIVATE_MSG_CMD_PREFIX);
    }

    public void sendServerMessage(String username, String message) throws IOException {
        sendMessage(username, message, SERVER_MSG_CMD_PREFIX);
    }

    public void sendMessage(String username, String message, String prefix) throws IOException {
        out.writeUTF(String.format("%s %s %s", prefix, username, message));
    }
}
