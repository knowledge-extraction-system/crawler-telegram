package kz.kaznu.telegram.client.utils;

public class Utils {

    public static Integer convertChatIdFromStringToInt(String chatId) {
        int chatIdAsInteger;
        try {
            chatIdAsInteger = Math.abs(Integer.parseInt(chatId));
        } catch (NumberFormatException e) {
            chatIdAsInteger = Integer.parseInt(chatId.replaceFirst("-1", ""));
        }
        return chatIdAsInteger;
    }

}
