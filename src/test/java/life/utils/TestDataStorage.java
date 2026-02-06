package life.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TestDataStorage {
    // Для Gradle используем build/test-data.txt
    private static final String FILE_PATH = "build/test-data.txt";
    private static Map<String, String> data = new HashMap<>();

    static {
        loadData();
    }

    // Сохраняем значение
    public static void save(String key, String value) {
        data.put(key, value);
        saveData();
        System.out.println("✅ Сохранено в TestDataStorage: " + key + " = " + value);
    }

    // Получаем значение
    public static String get(String key) {
        return data.get(key);
    }

    // Проверяем есть ли ключ
    public static boolean has(String key) {
        return data.containsKey(key);
    }

    // Сохраняем в файл
    private static void saveData() {
        try {
            // Создаем папку build если ее нет
            File file = new File(FILE_PATH);
            File parentDir = file.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (created) {
                    System.out.println("📁 Создана папка: " + parentDir.getAbsolutePath());
                }
            }

            // Сохраняем в простом формате
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    writer.println(entry.getKey() + "=" + entry.getValue());
                }
            }

            System.out.println("💾 Данные сохранены в: " + file.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("❌ Ошибка сохранения: " + e.getMessage());
            System.out.println("⚠️ Буду использовать только память");
        }
    }

    // Загружаем из файла
    private static void loadData() {
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                System.out.println("📂 Загружаю данные из: " + file.getAbsolutePath());

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            data.put(parts[0], parts[1]);
                        }
                    }
                }
                System.out.println("✅ Загружено " + data.size() + " записей");
            } else {
                System.out.println("📂 Файл данных не найден, начинаю с пустого хранилища");
            }
        } catch (IOException e) {
            System.err.println("❌ Ошибка загрузки: " + e.getMessage());
        }
    }

    // Очищаем данные
    public static void clear() {
        data.clear();
        saveData();
        System.out.println("🧹 TestDataStorage очищен");
    }

    // Выводим все данные
    public static void printAll() {
        System.out.println("\n📋 Содержимое TestDataStorage:");
        System.out.println("=".repeat(50));
        if (data.isEmpty()) {
            System.out.println("   (пусто)");
        } else {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                System.out.printf("   %-25s = %s%n", entry.getKey(), entry.getValue());
            }
        }
        System.out.println("=".repeat(50));
    }
}