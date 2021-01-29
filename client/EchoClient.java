package client;

import client.controllers.Controller;
import client.models.Network;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EchoClient extends Application {

    public static final List<String> USERS_TEST_DATA = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        USERS_TEST_DATA.add("Boris_Nikolaevich");
        USERS_TEST_DATA.add("Martin_Luther_Cat");
        USERS_TEST_DATA.add("Gandalf_the_Gray");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(EchoClient.class.getResource("views/sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Chat Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        Network network = new Network();
        network.connect();

        Controller controller = loader.getController();
        controller.setNetwork(network);

        network.waitMessage(controller);
    }
}
