package ru.tinkoff.semenov;

/**
 * Класс с полезными функциями для обработки сообщений и названий файлов
 */
public class Utils {

    /**
     * Метод парсинга сообщения для получения статуса ответа
     * @param message полученное сообщение
     * @return строка, которая может быть статусом ({@link ru.tinkoff.semenov.enums.Response})
     */
    public static String getStatus(String message) {
        int index = message.indexOf(Network.SEPARATOR);
        if (index == -1) {
            return message;
        }
        return message.substring(0, index);
    }

    /**
     * Метод парсинга сообщения для получения аргументов ответа
     * @param message полученное сообщение
     * @return список аргументов
     */
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

    /**
     * Собственно в моей файловой системе путь ведет либо к RegularFile, либо к директории, поэтому метод реализован
     * через отрицание {@link Utils#isRegularFile(String path)}
     * @param path путь к файлу
     * @return true - если это директория, false - если нет (обычный файл).
     */
    public static boolean isDirectory(String path) {
        return !isRegularFile(path);
    }
}
