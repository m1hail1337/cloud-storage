package ru.tinkoff.semenov;

import ru.tinkoff.semenov.Network;

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

    public static boolean isRegularFile(String path) {
        String regex = "^[a-zA-Z0-9-_!@#$%&^()?]+\\.[a-z]+$";
        return path.matches(regex);
    }

    public static boolean isDirectory(String path) {
        return !isRegularFile(path);
    }
}