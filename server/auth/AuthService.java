package server.auth;

public interface AuthService {
    String getUserNameByLoginAndPassword(String login, String password);

    void startAuthentication();

    void endAuthentication();
}
