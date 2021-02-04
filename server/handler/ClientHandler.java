package server.handler;

import server.Chat.MyServer;
import server.auth.AuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + pass
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/clientMsg"; // + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/serverMsg"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/w"; // sender/recipient + msg
    private static final String END_CMD_PREFIX = "/end"; //
    private static final String USERS_LIST_PREFIX = "/users"; //


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
            } catch (SocketTimeoutException e) {
                System.out.println("Время ожидания вышло");
                try {
                    clientSocket.close();
                } catch (IOException ioException) {
                    System.out.println("Socket не закрылся");
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                exclusionUserFromChat();
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
                String[] parts = message.split("\\s+", 3);
                String recipient = parts[1];
                String privatMessage = parts[2];
                myServer.privateMessage(privatMessage, this, recipient);
            } else {
                myServer.broadcastMessage(message, this);
            }
        }
    }

    private void addingUserToChat() {
        myServer.broadcastMessage(String.format(">>> %s присоединился к чату", userName),  this, true);
        myServer.subscribe(this);
        myServer.broadcastUserListMessage();
    }

    private void exclusionUserFromChat() {
        myServer.broadcastMessage(String.format(">>> %s покинул чат", userName),  this, true);
        myServer.unsubscribe(this);
        myServer.broadcastUserListMessage();
    }

    private void authentication() throws IOException {
        clientSocket.setSoTimeout(120000);
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
            clientSocket.setSoTimeout(0);
            out.writeUTF(AUTHOK_CMD_PREFIX + " " + userName);
            addingUserToChat();
            return true;
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Логин или пароль неверны");
            return false;
        }
    }

    public String getUserName() {
        return userName;
    }

    public void sendMessage(String username, String message, boolean isServerMessage) {
        if (isServerMessage){
            sendMessage(null, message, SERVER_MSG_CMD_PREFIX);
        } else{
            sendMessage(username, message, CLIENT_MSG_CMD_PREFIX);
        }
    }

    public void sendUserListMessage(String message) {
        sendMessage(null, message, USERS_LIST_PREFIX);
    }

    public void sendMessage(String username, String message, String prefix) {
        try {
            out.writeUTF(String.format("%s %s %s", prefix, username, message));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка отправки сообщения");
        }
    }
}
