package GB.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class Network {

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

    private static final int DEFAULT_SERVER_SOCKET = 8888;
    private static final String DEFAULT_SERVER_HOST = "localhost";
    private final int port;
    private final String host;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private static final Logger logger = LogManager.getLogger("ClientLogger");

    public Network(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public Network() {
        this.host = DEFAULT_SERVER_HOST;
        this.port = DEFAULT_SERVER_SOCKET;
    }

    public String getUsername() {
        return username;
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            logger.info("Соединение установлено");
        } catch (IOException e) {
            logger.error("Соединение не установлено");
        }
    }


    public void waitMessage(ChatController chatController) {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith(CLIENT_MSG_CMD_PREFIX)) {
                        String[] parts = message.split("\\s+", 3);
                        String sender = parts[1];
                        String messageFromUser = parts[2];
                        Platform.runLater(() -> chatController.appendMessage(String.format("%s: %s", sender, messageFromUser)));
                        logger.info("Пользователь " + sender + " прислал сообщение: " + messageFromUser);
                    } else if (message.startsWith(SERVER_MSG_CMD_PREFIX)) {
                        String[] parts = message.split("\\s+", 3);
                        String messageFromUser = parts[2];
                        Platform.runLater(() -> chatController.appendMessage(messageFromUser));
                        logger.info("Получено сообщение сервера: " + messageFromUser);
                    } else if (message.startsWith(USERS_LIST_PREFIX)) {
                        String[] parts = message.split("\\s+", 3);
                        String[] userList = parts[2].split("\\s+");
                        Platform.runLater(() -> chatController.setUsersList(userList));
                        logger.info("Получен список пользователей");
                    } else if (message.startsWith(NCKCHGOK_CMD_PREFIX)) {
                        String[] parts = message.split("\\s+", 3);
                        username = parts[1];
                        Platform.runLater(() -> chatController.setUsernameTitle(username));
                        logger.info("Имя пользователя изменено на " + username);
                    } else if (message.startsWith(NCKCHGERR_CMD_PREFIX)) {
                        String[] parts = message.split("\\s+", 2);
                        String error = parts[1];
                        Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Input Error");
                                    alert.setHeaderText("Ошибка смены имени");
                                    alert.setContentText(error);
                                    alert.show();
                                    chatController.setUsernameTitle(username);
                                }
                        );
                        logger.error("Ошибка смены имени пользователя: " + error);
                    } else {
                        logger.error("Неизвестная ошибка сервера");
                    }
                }
            } catch (IOException e) {
                logger.error("Ошибка подключения");
            }

        });

        thread.setDaemon(true);
        thread.start();
    }

    public String sendAuthCommand(String login, String password) {
        return sendCommand(String.format("%s %s %s", AUTH_CMD_PREFIX, login, password));
    }

    public String sendRegCommand(String login, String username, String password) {
        return sendCommand(String.format("%s %s %s %s", REG_CMD_PREFIX, login, username, password));
    }

    public void sendChangeUsernameCommand(String newUsername) throws IOException {
        sendMessage(String.format("%s %s", NCKCHG_CMD_PREFIX, newUsername));
    }

    public String sendCommand(String command) {
        try {
            out.writeUTF(command);
            logger.info("Отправка сообщения: " + command);
            String response = in.readUTF();
            logger.info("Получено сообщение: " + response);
            if (response.startsWith(AUTHOK_CMD_PREFIX)) {
                this.username = response.split("\\s+", 2)[1];
                logger.info("Авторизация прошла успешно");
                return null;
            } else {
                String error = response.split("\\s+", 2)[1];
                logger.warn("Ошибка авторизации: " + error);
                return error;
            }
        } catch (EOFException e) {
            logger.error("Ошибка получения сообщения");
            return "Ошибка получения сообщения";
        } catch (IOException e) {
            logger.error("Ошибка ввода/вывода");
            return "Ошибка ввода/вывода";
        }
    }

    public void sendMessage(String message) throws IOException {
        logger.info("Отправка сообщения: " + message);
        out.writeUTF(message);
    }

    public void sendPrivateMessage(String message, String recipient) throws IOException {
        String command = String.format("%s %s %s", PRIVATE_MSG_CMD_PREFIX, recipient, message);
        sendMessage(command);
    }


}
