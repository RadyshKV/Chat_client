package GB.client;

import java.io.*;
import java.util.ArrayList;

public class History {
    private final String fileHistory;

    public History(String login) {
        this.fileHistory = "src/main/resources/GB/client/history_" + login + ".txt";
    }


    ArrayList<String> readHistory() {
        ArrayList<String> historyStrings = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileHistory))) {
            String str;
            while ((str = reader.readLine()) != null) {
                historyStrings.add(str);
            }
        } catch (IOException e) {
            System.out.println("Ошибка открытия файла");
        }
        return historyStrings;
    }

    void writeHistory(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileHistory, true))) {
            writer.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
