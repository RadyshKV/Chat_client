package GB.server;

import GB.server.Chat.MyServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ServerApp {

    private static final int DEFAULT_PORT = 8888;
    private static final Logger logger = LogManager.getLogger("ServerLogger");

    public static void main(String[] args) {

        int port = DEFAULT_PORT;
        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

        try {
            new MyServer(port).start();
        } catch (IOException e) {
            logger.error("Ошибка запуска сервера");
            System.exit(1);
        }
    }


}
