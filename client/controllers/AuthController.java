package client.controllers;

import client.ChatGB;
import client.models.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AuthController {

    @FXML
    private TextField passwordField;

    @FXML
    private TextField loginField;

    private Network network;
    private ChatGB mainChatGB;

    @FXML
    void checkAuth(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.length() == 0 || password.length() == 0) {
            System.out.println("Поля не должны быть пустыми!!!");
            return;
        }

        String authErrorMessage = network.sendAuthCommand(login, password);
        if (authErrorMessage == null){
            mainChatGB.openChat();
        } else {
            System.out.println("Ошибка аутентификации: " + authErrorMessage);
        }

    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setChat(ChatGB networkChat) {
        this.mainChatGB = networkChat;
    }
}