package GB.client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class RegController {


    @FXML
    private TextField loginField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    private Network network;

    private ChatGB mainChatGB;

    @FXML
    void applyNewNick() {
        String login = loginField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.length() == 0 || username.length() == 0 || password.length() == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText("Ошибка ввода");
            alert.setContentText("Поля не должны быть пустыми");
            alert.show();
            return;
        }

        String errorMessage = network.sendRegCommand(login, username, password);
        if (errorMessage == null) {
            mainChatGB.openAuthDialog();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Error");
            alert.setHeaderText("Ошибка регистрации");
            alert.setContentText(errorMessage);
            alert.show();
        }
        loginField.clear();
        passwordField.clear();
        usernameField.clear();
    }


    @FXML
    void cancel() {
        loginField.clear();
        passwordField.clear();
        usernameField.clear();
        mainChatGB.openAuthDialog();
    }


    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setChat(ChatGB networkChat) {
        this.mainChatGB = networkChat;
    }

}
