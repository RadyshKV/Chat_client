package server.auth;

import server.Chat.User;

import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {

    public static final List<User> clients = new ArrayList<>();

    public BaseAuthService() {
        clients.add(new User("martin", "1111", "Мартин_Некотов"));
        clients.add(new User("boris", "2222", "Борис_Николаевич"));
        clients.add(new User("gena", "3333", "Гендальф_серый"));
    }

    @Override
    public String getUserNameByLoginAndPassword(String login, String password) {
        for (User client : clients) {
            if (client.getLogin().equals(login) & client.getPassword().equals(password)) {
                return client.getUsername();
            }
        }
        return null;
    }

    @Override
    public void startAuthentication() {
        System.out.println("Старт аутентификации");
    }

    @Override
    public void endAuthentication() {
        System.out.println("Окончание аутентификации");
    }
}
