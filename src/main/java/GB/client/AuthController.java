package GB.client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthController {

    @FXML
    private TextField passwordField;

    @FXML
    private TextField loginField;

    private Network network;
    private ChatGB mainChatGB;


    @FXML
    void checkAuth() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.length() == 0 || password.length() == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText("Ошибка ввода");
            alert.setContentText("Поля не должны быть пустыми");
            alert.show();
            return;
        }

        String authErrorMessage = network.sendAuthCommand(login, password);
        if (authErrorMessage == null) {
            mainChatGB.openChat(login);
        } else {
            loginField.clear();
            passwordField.clear();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Authorization Error");
            alert.setHeaderText("Ошибка авторизации");
            alert.setContentText(authErrorMessage);
            alert.show();
        }

    }

    @FXML
    void register() {
        loginField.clear();
        passwordField.clear();
        mainChatGB.openRegDialog();
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setChat(ChatGB networkChat) {
        this.mainChatGB = networkChat;
    }
}