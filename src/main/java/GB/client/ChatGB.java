package GB.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ChatGB extends Application {

    private Network network;
    private Stage primaryStage;
    private Stage authStage;
    private Stage regStage;
    private ChatController chatController;
    private RegController regController;
    private AuthController authController;
    private History history;
    private static final Logger logger = LogManager.getLogger("ClientLogger");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.primaryStage = primaryStage;
        network = new Network();
        network.connect();
        createAndOpenAuthDialog();
        createChatDialog();
        createRegDialog();
        logger.info("Клиент запущен");
    }

    private void createChatDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ChatGB.class.getResource("chat-view.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Messenger");
        primaryStage.setScene(new Scene(root));
        chatController = loader.getController();
        chatController.setNetwork(network);
    }

    private void createAndOpenAuthDialog() throws IOException {
        FXMLLoader authLoader = new FXMLLoader();
        authLoader.setLocation(ChatGB.class.getResource("auth-view.fxml"));
        Parent root = authLoader.load();
        authStage = new Stage();

        authStage.setTitle("Authentication");
        authStage.setScene(new Scene(root));
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.initOwner(primaryStage);
        authStage.show();
        authController = authLoader.getController();
        authController.setNetwork(network);
        authController.setChat(this);
        logger.info("Авторизация");
    }

    public void openChat(String login) {
        logger.info("Авторизация пройдена");
        authStage.close();
        regStage.close();
        primaryStage.show();
        primaryStage.setTitle("ЧАТ");
        network.waitMessage(chatController);
        chatController.setUsernameTitle(network.getUsername());
        history = new History(login);
        chatController.setHistory(history);
        chatController.loadHistory();
    }

    private void createRegDialog() throws IOException {
        FXMLLoader regLoader = new FXMLLoader();
        regLoader.setLocation(ChatGB.class.getResource("reg-view.fxml"));
        Parent root = regLoader.load();
        regStage = new Stage();

        regStage.setTitle("Registration");
        regStage.setScene(new Scene(root));
        regStage.initModality(Modality.WINDOW_MODAL);
        regStage.initOwner(authStage);
        regController = regLoader.getController();
        regController.setNetwork(network);
        regController.setChat(this);
    }

    public void openAuthDialog() {
        logger.info("Авторизация");
        regStage.close();
        authStage.show();
    }

    public void openRegDialog() {
        logger.info("Регистрация");
        authStage.close();
        regStage.show();
    }
}
