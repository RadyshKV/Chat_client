package GB.client;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ChatController {

    private final int chatHistoryLimit = 100;
    @FXML
    private TextField inputField;
    @FXML
    private ListView<String> usersList;
    @FXML
    private TextField usernameTitle;
    @FXML
    private Label messageTo;
    @FXML
    private TextArea chatHistory;
    private Network network;
    private String selectedRecipient;
    private History history;

    public void setHistory(History history) {
        this.history = history;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setUsernameTitle(String username) {
        this.usernameTitle.setText(username);
    }

    public void setUsersList(String[] usersList) {
        this.usersList.setItems(FXCollections.observableArrayList(usersList));
    }

    @FXML
    void initialize() {
        usersList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = usersList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                usersList.requestFocus();
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedRecipient = null;
                        messageTo.setText("Сообщение для всех");
                    } else {
                        selectionModel.select(index);
                        selectedRecipient = cell.getItem();
                        messageTo.setText("Сообщение для " + selectedRecipient);
                    }
                    event.consume();
                }

            });
            return cell;
        });
    }


    @FXML
    void sendMessage() {
        String message = inputField.getText().trim();
        if (message.length() != 0) {
            appendMessage("Я: " + message);
            try {
                if (selectedRecipient != null) {
                    network.sendPrivateMessage(message, selectedRecipient);
                } else {
                    network.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Ошибка при отправке сообщения");
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText("Ошибка ввода сообщения");
            alert.setContentText("Нельзя отправлять пустое сообщение");
            alert.show();
        }
        inputField.clear();
    }

    void appendMessage(String message) {
        String timeStamp = DateFormat.getInstance().format(new Date());
        chatHistory.appendText(timeStamp);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(message);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(System.lineSeparator());

        history.writeHistory(timeStamp + System.lineSeparator() + message + System.lineSeparator() + System.lineSeparator());

    }

    @FXML
    void changeUsername() {
        String message = usernameTitle.getText().trim();
        if (message.length() != 0) {
            try {
                network.sendChangeUsernameCommand(message);
            } catch (IOException e) {
                System.out.println("Ошибка смены имени");
                usernameTitle.setText(network.getUsername());
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText("Ошибка ввода");
            alert.setContentText("Поле не может быть пустым");
            alert.show();
            usernameTitle.setText(network.getUsername());
        }
    }

    void loadHistory() {
        ArrayList<String> messageHistory = history.readHistory();
        if (messageHistory != null) {
            int shift = messageHistory.size() >= chatHistoryLimit ? messageHistory.size() - chatHistoryLimit : 0;
            for (int i = shift; i < messageHistory.size(); i++) {
                chatHistory.appendText(messageHistory.get(i));
                chatHistory.appendText(System.lineSeparator());
            }
        }
    }

    @FXML
    void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Chat Client");
        alert.setContentText("Клинтская программа сетевого чата");
        alert.show();
    }

    @FXML
    void exit() {
        System.exit(0);
    }


}

