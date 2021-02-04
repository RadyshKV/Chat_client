package client;

import client.controllers.AuthController;
import client.controllers.ChatController;
import client.models.Network;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatGB extends Application {

    public static final List<String> USERS_TEST_DATA = new ArrayList<>();
    private Network network;
    private Stage primaryStage;
    private Stage authStage;
    private ChatController chatController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.primaryStage = primaryStage;
        network = new Network();
        network.connect();
        openAuthDialog();
        createChatDialog();
    }

    private void createChatDialog() throws IOException {
        USERS_TEST_DATA.add("Мартин_Некотов");
        USERS_TEST_DATA.add("Борис_Николаевич");
        USERS_TEST_DATA.add("Гендальф_серый");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ChatGB.class.getResource("views/chat-view.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Messenger");
        primaryStage.setScene(new Scene(root));
        chatController = loader.getController();
        chatController.setNetwork(network);
    }

    private void openAuthDialog() throws IOException {
        FXMLLoader authLoader = new FXMLLoader();
        authLoader.setLocation(ChatGB.class.getResource("views/auth-view.fxml"));
        Parent root = authLoader.load();
        authStage = new Stage();

        authStage.setTitle("Authentication");
        authStage.setScene(new Scene(root));
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.initOwner(primaryStage);
        authStage.show();
        AuthController authController = authLoader.getController();
        authController.setNetwork(network);
        authController.setChat(this);

    }

    public void openChat(){
        authStage.close();
        primaryStage.show();
        primaryStage.setTitle(network.getUsername());
        primaryStage.setAlwaysOnTop(true);
        network.waitMessage(chatController);
        chatController.setUsernameTitle(network.getUsername());
    }
}
