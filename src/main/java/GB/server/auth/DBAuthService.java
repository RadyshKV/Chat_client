package GB.server.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DBAuthService implements AuthService {

    public static Connection connection;
    public static Statement stmt;
    public static ResultSet rs;
    private static final Logger logger = LogManager.getLogger("ServerLogger");

    public DBAuthService() {
        try {
            connection();
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("соединение с DB не установлено");
        }

    }


    void connection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/GB/server/database.db");
        stmt = connection.createStatement();
    }

    void disconnection() throws SQLException {

        connection.close();
    }

    @Override
    public String getUserNameByLoginAndPassword(String login, String password) {
        try {
            rs = stmt.executeQuery(String.format("SELECT password, username FROM users WHERE login = '%s'", login));
            String username = rs.getString("username");
            if (rs.getString("password").equals(password)) {
                return username;
            } else {
                return null;
            }
        } catch (SQLException e) {
            logger.warn(String.format("Пользователя с login = %s в DB не существует%n", login));
            return null;
        }
    }

    @Override
    public boolean updateUsername(String newUsername, String oldUsername) {
        try {
            stmt.executeUpdate(String.format("UPDATE users SET username = '%s' WHERE username = '%s'", newUsername, oldUsername));
            logger.info("Имя обновлено");
            return true;
        } catch (SQLException e) {
            logger.warn(String.format("Не удалось обновить имя пользователя '%s' на '%s'", oldUsername, newUsername));
            return false;
        }
    }

    @Override
    public boolean newUserRegister(String login, String username, String password) {
        try {
            stmt.executeUpdate(String.format("INSERT INTO users (login, password, username) VALUES ('%s', '%s', '%s')", login, password, username));
            return true;
        } catch (SQLException e) {
            logger.warn("Не удалось добавить нового пользователя " + username);
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
