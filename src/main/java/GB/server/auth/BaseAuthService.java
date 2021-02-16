package GB.server.auth;

import java.sql.*;

public class BaseAuthService implements AuthService {

    public static Connection connection;
    public static Statement stmt;
    public static ResultSet rs;


    public BaseAuthService() {
        try {
            connection();
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("соединение с DB не установлено");
        }

    }


    void connection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/GB/server/database.db");
        stmt = connection.createStatement();
    }

    void disconnect() throws SQLException {
        connection.close();
    }

    @Override
    public String getUserNameByLoginAndPassword(String login, String password) {
        try {
            rs = stmt.executeQuery(String.format("SELECT username FROM users WHERE login = '%s' AND password = '%s'", login, password));
            return rs.getString("username");
        } catch (SQLException e) {
            System.out.printf("Пользователя с login = %s и password = %s в DB не существует%n", login, password);
            return null;
        }
    }

    @Override
    public boolean updateUsername(String newUsername, String oldUsername) {
        try {
            stmt.executeUpdate(String.format("UPDATE users SET username = '%s' WHERE username = '%s'", newUsername, oldUsername));
            System.out.println("Имя обновлено");
            return true;
        } catch (SQLException e) {
            System.out.println("Не удалось обновить имя пользователя");
            return false;
        }
    }

    @Override
    public boolean newUserRegister(String login, String username, String password) {
        try {
            stmt.executeUpdate(String.format("INSERT INTO users (login, password, username) VALUES ('%s', '%s', '%s')", login, password, username));
            return true;
        } catch (SQLException e) {
            System.out.println("Не удалось добавить нового пользователя");
            return false;
        }
    }

    @Override
    public void startAuthentication() {

    }

    @Override
    public void endAuthentication() {

    }
}
