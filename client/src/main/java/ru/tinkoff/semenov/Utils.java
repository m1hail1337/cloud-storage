package ru.tinkoff.semenov;

public class Utils {

    public static String getStatus(String message) {
        int index = message.indexOf(Network.SEPARATOR);
        if (index == -1) {
            return message;
        }
        return message.substring(0, index);
    }

    public static String[] getArgs(String message) {
        int index = message.indexOf(Network.SEPARATOR);
        if (index == -1) {
            return new String[0];
        }
        return message.substring(index + 1).split("\\|");
    }

    /**
     * Метод, проверяющий обычный ли это файл по его имени. Есть ошибки со специфичными расширениями (типа .7z)
     * @param path путь к файлу
     * @return true - если это обычный файл, false - если нет (директория).
     */
    public static boolean isRegularFile(String path) {
        String regex = "^[а-яА-Яa-zA-Z0-9-_!@#$%&^()?\\s.]+\\.[a-z]+$";
        return path.matches(regex);
    }

    public static boolean isDirectory(String path) {
        return !isRegularFile(path);
    }
}
