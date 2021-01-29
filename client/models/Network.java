package client.models;

import client.controllers.Controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private static final String AUTH_CMD_PREFIX = "/auth";
    private static final String AUTHOK_CMD_PREFIX = "/authok";
    private static final String AUTHERR_CMD_PREFIX = "/autherr";
    private static final String CLIENT_MSG_CMD_PREFIX = "/clientMsg";
    private static final String SERVER_MSG_CMD_PREFIX = "/serverMsg";
    private static final String PRIVATE_MSG_CMD_PREFIX = "/w";
    private static final String END_CMD_PREFIX = "/end";

    private static final int DEFAULT_SERVER_SOCKET = 8888;
    private static final String DEFAULT_SERVER_HOST = "localhost";

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final int port;
    private final String host;

    public Network(String host, int port) {
        this.port = port;
        this.host = host;
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


    public void waitMessage(Controller controller) {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    controller.appendMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Ошибка подключения");
            }

        });

        thread.setDaemon(true);
        thread.start();
    }
}
