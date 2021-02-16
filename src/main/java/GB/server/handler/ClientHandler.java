package GB.server.handler;

import GB.server.Chat.MyServer;
import GB.server.auth.AuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + pass
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
    private static final String NCKCHG_CMD_PREFIX = "/nckchg"; // + newUsername
    private static final String NCKCHGOK_CMD_PREFIX = "/nckchgok"; // + newUsername
    private static final String NCKCHGERR_CMD_PREFIX = "/nckchgerr"; // + error message
    private static final String REG_CMD_PREFIX = "/reg"; // + login + username + password
    private static final String CLIENT_MSG_CMD_PREFIX = "/clientMsg"; // + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/serverMsg"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/w"; // sender/recipient + msg
    private static final String END_CMD_PREFIX = "/end";
    private static final String USERS_LIST_PREFIX = "/users"; //

    private final MyServer myServer;
    private final Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;


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
            System.out.println("message| " + username + ": " + message);
            if (message.startsWith(END_CMD_PREFIX)) {
                return;
            } else if (message.startsWith(PRIVATE_MSG_CMD_PREFIX)) {
                String[] parts = message.split("\\s+", 3);
                String recipient = parts[1];
                String privatMessage = parts[2];
                myServer.privateMessage(privatMessage, this, recipient);
            } else if (message.startsWith(NCKCHG_CMD_PREFIX)) {
                String newUsername = updateAndGetUsername(message);
                if (newUsername != null) {
                    out.writeUTF(NCKCHGOK_CMD_PREFIX + " " + newUsername);
                    updateUsernameInChat(newUsername);
                } else {
                    out.writeUTF(NCKCHGERR_CMD_PREFIX + " Недопустимое имя");
                }
            } else {
                myServer.broadcastMessage(message, this);
            }
        }
    }

    private void updateUsernameInChat(String newUsername) {
        myServer.broadcastMessage(String.format(">>> %s изменил имя на %s", username, newUsername), this, true);
        username = newUsername;
        myServer.broadcastUserListMessage();
    }

    private void addingUserToChat() {
        myServer.broadcastMessage(String.format(">>> %s присоединился к чату", username), this, true);
        myServer.subscribe(this);
        myServer.broadcastUserListMessage();
    }

    private void exclusionUserFromChat() {
        myServer.broadcastMessage(String.format(">>> %s покинул чат", username), this, true);
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
            } else if (message.startsWith(REG_CMD_PREFIX)) {
                processRegCommand(message);
            } else {
                out.writeUTF(AUTHERR_CMD_PREFIX + " Ошибка авторизации");
            }
        }
    }

    private void processRegCommand(String message) throws IOException {
        String[] parts = message.split("\\s+", 5);
        String login = parts[1];
        String username = parts[2];
        String password = parts[3];
        AuthService authService = myServer.getAuthService();
        if (authService.newUserRegister(login, username, password)) {
            out.writeUTF(AUTHOK_CMD_PREFIX + " " + username);
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Логин или имя заняты");
        }
    }


    private String updateAndGetUsername(String message) {
        String[] parts = message.split("\\s+", 3);
        String newUsername = parts[1];
        AuthService authService = myServer.getAuthService();
        if (authService.updateUsername(newUsername, username)) {
            return newUsername;
        }
        return null;
    }


    private boolean processAuthCommand(String message) throws IOException {
        String[] parts = message.split("\\s+", 3);
        String login = parts[1];
        String password = parts[2];

        AuthService authService = myServer.getAuthService();
        username = authService.getUserNameByLoginAndPassword(login, password);
        if (username != null) {
            if (myServer.isUserNameBusy(username)) {
                out.writeUTF(AUTHERR_CMD_PREFIX + " Логин занят");
                return false;
            }
            clientSocket.setSoTimeout(0);
            out.writeUTF(AUTHOK_CMD_PREFIX + " " + username);
            addingUserToChat();
            return true;
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Логин или пароль неверны");
            return false;
        }
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(String username, String message, boolean isServerMessage) {
        if (isServerMessage) {
            sendMessage(null, message, SERVER_MSG_CMD_PREFIX);
        } else {
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
