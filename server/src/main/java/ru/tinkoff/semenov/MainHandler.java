package ru.tinkoff.semenov;

import static io.netty.channel.ChannelHandler.Sharable;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.tinkoff.semenov.commands.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Серверный обработчик сообщений. Аннотация {@link Sharable} позволяет сохранить текущий хендлер и переключится на него
 * после загрузки файла.
 */
@Sharable
public class MainHandler extends SimpleChannelInboundHandler<String> {

    /**
     * Путь к файлу с логинами и паролями пользователей
     */
    private static final String PATH_TO_AUTH_DATA = "server\\src\\main\\resources\\users.txt";

    /**
     * Путь к папке с директориями всех существующих пользователей
     */
    private static final String PATH_TO_USERS_DATA ="server\\src\\main\\resources\\dirs";

    /**
     * Разделитель между командой и аргументами сообщения. NB: изменять только в рамках
     * изменения политики передачи данных между сервером и клиентами
     */
    public static final String SEPARATOR = "|";

    /**
     * Мапа пользователей в формате логин-пароль
     */
    private static final Map<String, String> users = new HashMap<>();

    /**
     * Мапа названий команд и их действий
     */
    private final Map<String, Command> commands = new HashMap<>() {{
        put("AUTH", new AuthCommand());
        put("REGISTER", new RegisterCommand());
        put("NEW_DIR", new NewDirCommand());
        put("DELETE", new DeleteCommand());
        put("CUT", new CutCommand());
        put("COPY", new CopyCommand());
        put("LOAD", new LoadCommand());
        put("FILE_LENGTH", new FileLengthCommand(MainHandler.this));
        put("DOWNLOAD", new DownloadCommand(MainHandler.this));
    }};

    /**
     * Текущий контекст канала
     */
    private ChannelHandlerContext context;

    /**
     * Последний запрошенный на скачивание файл
     */
    private File currentDownloadFile;


    /**
     * Метод срабатывающий сразу при подключении. В нем мы инициализируем данные о существующих пользователях
     * для успешной процедуры авторизации
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        initUsersMap();
    }

    /**
     * Метод обрабатывающий команду пользователя и отправляющий ответ
     * @param msg полученное сообщение
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        this.context = ctx;
        String command = msg.substring(0, msg.indexOf(SEPARATOR));
        String args = msg.substring(msg.indexOf(SEPARATOR) + 1);
        String response = commands.get(command).execute(args);

        if (command.equals("LOAD")) {
            switchToFileHandler(ctx, args);
        }

        if (!response.equals(Response.EMPTY.name())) {
            ctx.channel().writeAndFlush(response);
        }
    }

    /**
     * Метод используемый при ошибках. Закрывает контекст передачи и выводит логи случившейся неполадки
     * @param ctx контекст обработчика
     * @param cause случившаяся ошибка
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        throw new RuntimeException("Ошибка сервера", cause);
    }

    /**
     * Инициализация мапы пользователей ({@link  #users}) из файлика {@link #PATH_TO_AUTH_DATA}
     */
    private void initUsersMap() {
        Path filePath = Paths.get(PATH_TO_AUTH_DATA);
        try (Stream<String> lines = Files.lines(filePath)) {
            users.clear();
            users.putAll(lines.collect(Collectors.toMap(k -> k.split("\\s")[0], v -> v.split("\\s")[1])));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл: " + PATH_TO_AUTH_DATA, e);
        }
    }

    /**
     * @param login логин пользователя
     * @return информация из {@link #PATH_TO_USERS_DATA} о путях ко всем файлам и директориям пользователя, разделенная {@link  #SEPARATOR}
     */
    public static String getUserDirs(String login) {
        Path root = Paths.get(PATH_TO_USERS_DATA + "/" + login);
        StringBuilder pathsString = new StringBuilder();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.forEach(path -> pathsString
                    .append(path.toString().replace(PATH_TO_USERS_DATA, ""))
                    .append(SEPARATOR));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pathsString.toString();
    }

    /**
     * Метод подготавливает канал к получению файлового потока: удаляет строковые кодеры/декодеры
     * ({@link io.netty.handler.codec.string.StringEncoder}
     * и {@link io.netty.handler.codec.string.StringDecoder}) из конвейера и заменяет стандартный хендлер на файловый
     * @param ctx текущий контекст
     * @param args директория, куда будет производиться запись нового файла и размер файла (через {@link #SEPARATOR})
     */
    private void switchToFileHandler(ChannelHandlerContext ctx, String args) {
        String filePath = args.substring(0, args.indexOf(SEPARATOR));
        long fileLength = Long.parseLong(args.substring(args.indexOf(SEPARATOR) + 1));
        ctx.pipeline().replace("defaultHandler", "fileHandler",
                new ServerFileHandler(PATH_TO_USERS_DATA + "\\" + filePath, fileLength, this));
        ctx.pipeline().remove("stringDecoder");
        ctx.pipeline().remove("stringEncoder");
    }

    public static String getPathToAuthData() {
        return PATH_TO_AUTH_DATA;
    }

    public static String getPathToUsersData() {
        return PATH_TO_USERS_DATA;
    }

    public static Map<String, String> getUsers() {
        return users;
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public void setCurrentDownloadFile(File currentDownloadFile) {
        this.currentDownloadFile = currentDownloadFile;
    }

    public File getCurrentDownloadFile() {
        return currentDownloadFile;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }
}
