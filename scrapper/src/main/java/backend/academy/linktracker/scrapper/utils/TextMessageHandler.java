package backend.academy.linktracker.scrapper.utils;

public class TextMessageHandler {

    private static final int MAX_MESSAGE_LENGTH = 200;

    private TextMessageHandler() {}
    ;

    public static String ShortenMessage(String message) {

        if (message.length() > MAX_MESSAGE_LENGTH) {
            return message.substring(0, MAX_MESSAGE_LENGTH - 3) + "...";
        }

        return message;
    }
}
