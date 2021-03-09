package GB.server.auth;

public interface AuthService {
    String getUserNameByLoginAndPassword(String login, String password);

    boolean updateUsername(String login, String username);

    boolean newUserRegister(String login, String username, String password);

    void startAuthentication();

    void endAuthentication();
}
