package kz.kaznu.telegram.client.services;

import kz.kaznu.telegram.client.repositories.TelegramChatService;
import kz.kaznu.telegram.client.repositories.TelegramMessageService;
import kz.kaznu.telegram.client.repositories.TelegramUserService;
import kz.kaznu.telegram.client.tdlib.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class UpdatesHandler implements Client.ResultHandler {

    private String phoneNumber;
    private String apiHash;
    private int apiId;
    private String proxyIp;
    private int proxyPort;
    private String proxyUserName;
    private String proxyPassword;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private static Client client;
    private final Lock authorizationLock = new ReentrantLock();
    private final Condition gotAuthorization = authorizationLock.newCondition();

    @Autowired
    private TelegramChatService telegramChatService;
    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private TelegramMessageService telegramMessageService;

    public UpdatesHandler(@Value("${telegram.phone.number}") String phoneNumber, @Value("${telegram.api.hash}") String apiHash,
                          @Value("${telegram.api.key}") int apiId, @Value("${telegram.proxy.ip}") String proxyIp,
                          @Value("${telegram.proxy.port}") int proxyPort, @Value("${telegram.proxy.username}") String proxyUserName,
                          @Value("${telegram.proxy.password}") String proxyPassword) throws InterruptedException, IllegalAccessException {
        this.phoneNumber = phoneNumber;
        this.apiHash = apiHash;
        this.apiId = apiId;

        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
        this.proxyUserName = proxyUserName;
        this.proxyPassword = proxyPassword;

        try {
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError  e) {
            LOGGER.error(e.getMessage() + "\n" + e.toString());
        }

        String[] libraries = getLoadedLibraries(ClassLoader.getSystemClassLoader());
        for (String library : libraries) {
            LOGGER.info("Library: " + library);
        }

        client = Client.create(this, null, null);
    }

    private static java.lang.reflect.Field LIBRARIES;
    static {
        try {
            LIBRARIES = ClassLoader.class.getDeclaredField("loadedLibraryNames");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        LIBRARIES.setAccessible(true);
    }

    public static String[] getLoadedLibraries(final ClassLoader loader) throws IllegalAccessException {
        final Vector<String> libraries = (Vector<String>) LIBRARIES.get(loader);
        return libraries.toArray(new String[] {});
    }

    public static Client getClient() {
        return client;
    }

    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                LOGGER.info("UpdateAuthorizationState");
                onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                break;
            case TdApi.UpdateUser.CONSTRUCTOR:
                TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                TdApi.User user = updateUser.user;
                LOGGER.info("UpdateUser\n" + user.toString());
                telegramUserService.saveIfNewTelegramUser(user);
                break;
            case TdApi.UpdateUserStatus.CONSTRUCTOR:  {
                LOGGER.info("UpdateUserStatus");
                TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
                LOGGER.info("UpdateUserStatus\n" + updateUserStatus.toString());
                //TODO: get user by id from db and update status
                break;
            }
            case TdApi.UpdateBasicGroup.CONSTRUCTOR:
                LOGGER.info("UpdateBasicGroup");
                TdApi.UpdateBasicGroup updateBasicGroup = (TdApi.UpdateBasicGroup) object;
                LOGGER.info("UpdateBasicGroup\n" + updateBasicGroup.toString());
                //TODO: save basicGroup to db
                break;
            case TdApi.UpdateSupergroup.CONSTRUCTOR:
                LOGGER.info("UpdateSuperGroup");
                TdApi.UpdateSupergroup updateSuperGroup = (TdApi.UpdateSupergroup) object;
                LOGGER.info("UpdateSuperGroup\n" + updateSuperGroup.toString());
                //TODO: save superGroup to db
                break;
            case TdApi.UpdateSecretChat.CONSTRUCTOR:
                LOGGER.info("UpdateSecretChat");
                TdApi.UpdateSecretChat updateSecretChat = (TdApi.UpdateSecretChat) object;
                //TODO: save secretChat to db
                break;
            case TdApi.UpdateNewChat.CONSTRUCTOR: {
                TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
                TdApi.Chat chat = updateNewChat.chat;
                LOGGER.info("UpdateNewChat\n" + chat.toString());
                telegramChatService.saveIfNewTelegramChat(chat);
                break;
            }
            case TdApi.UpdateChatTitle.CONSTRUCTOR: {
                LOGGER.info("UpdateChatTitle");
                TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
                LOGGER.info("UpdateChatTitle\n" + updateChat.toString());
                //TODO: db
                break;
            }
            case TdApi.UpdateChatPhoto.CONSTRUCTOR: {
                LOGGER.info("UpdateChatPhoto");
                TdApi.UpdateChatPhoto updateChat = (TdApi.UpdateChatPhoto) object;
                //TODO: db
                break;
            }
//            case TdApi.UpdateChatChatList.CONSTRUCTOR: {
//                LOGGER.info("UpdateChatChatList");
//                TdApi.UpdateChatChatList updateChat = (TdApi.UpdateChatChatList) object;
//                LOGGER.info("UpdateChatChatList\n" + updateChat.toString());
//                //TODO: db
//                break;
//            }
            case TdApi.UpdateChatLastMessage.CONSTRUCTOR: {
                LOGGER.info("UpdateChatLastMessage");
                TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
                LOGGER.info("UpdateChatLastMessage\n" + updateChat.toString());
                //TODO: db
                break;
            }
//            case TdApi.UpdateChatOrder.CONSTRUCTOR: {
//                LOGGER.info("UpdateChatOrder");
//                TdApi.UpdateChatOrder updateChat = (TdApi.UpdateChatOrder) object;
//                //TODO: db
//                break;
//            }
//            case TdApi.UpdateChatIsPinned.CONSTRUCTOR: {
//                LOGGER.info("UpdateChatIsPinned");
//                TdApi.UpdateChatIsPinned updateChat = (TdApi.UpdateChatIsPinned) object;
//                //TODO: db
//                break;
//            }
            case TdApi.UpdateChatReadInbox.CONSTRUCTOR: {
                LOGGER.info("UpdateChatReadInbox");
                TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
                //TODO: db
                break;
            }
            case TdApi.UpdateChatReadOutbox.CONSTRUCTOR: {
                LOGGER.info("UpdateChatReadOutbox");
                TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
                //TODO: db
                break;
            }
            case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR: {
                LOGGER.info("UpdateChatUnreadMentionCount");
                TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
                //TODO: db
                break;
            }
            case TdApi.UpdateMessageMentionRead.CONSTRUCTOR: {
                LOGGER.info("UpdateMessageMentionRead");
                TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
                //TODO: db
                break;
            }
            case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: {
                LOGGER.info("UpdateChatReplyMarkup");
                TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
                //TODO: db
                break;
            }
            case TdApi.UpdateChatDraftMessage.CONSTRUCTOR: {
                LOGGER.info("UpdateChatDraftMessage");
                TdApi.UpdateChatDraftMessage updateChat = (TdApi.UpdateChatDraftMessage) object;
                //TODO: db
                break;
            }
            case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR: {
                LOGGER.info("UpdateChatNotificationSettings");
                TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
                //TODO: db
                break;
            }
            case TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR: {
                LOGGER.info("UpdateChatDefaultDisableNotification");
                TdApi.UpdateChatDefaultDisableNotification update = (TdApi.UpdateChatDefaultDisableNotification) object;
                //TODO: db
                break;
            }
            case TdApi.UpdateChatIsMarkedAsUnread.CONSTRUCTOR: {
                LOGGER.info("UpdateChatIsMarkedAsUnread");
                TdApi.UpdateChatIsMarkedAsUnread update = (TdApi.UpdateChatIsMarkedAsUnread) object;
                //TODO: db
                break;
            }
//            case TdApi.UpdateChatIsSponsored.CONSTRUCTOR: {
//                LOGGER.info("UpdateChatIsSponsored");
//                TdApi.UpdateChatIsSponsored updateChat = (TdApi.UpdateChatIsSponsored) object;
//                //TODO: db
//                break;
//            }

            case TdApi.UpdateUserFullInfo.CONSTRUCTOR:
                TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
                TdApi.UserFullInfo userFullInfo = updateUserFullInfo.userFullInfo;
                LOGGER.info("UpdateUserFullInfo:\n" + userFullInfo.toString());
                //TODO: db
                break;
            case TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR:
                LOGGER.info("UpdateBasicGroupFullInfo");
                TdApi.UpdateBasicGroupFullInfo updateBasicGroupFullInfo = (TdApi.UpdateBasicGroupFullInfo) object;
                //TODO: db
                break;
            case TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR:
                LOGGER.info("UpdateSupergroupFullInfo");
                TdApi.UpdateSupergroupFullInfo updateSupergroupFullInfo = (TdApi.UpdateSupergroupFullInfo) object;
                //TODO: db
                break;
            case TdApi.UpdateNewMessage.CONSTRUCTOR:
                TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) object;
                TdApi.Message message = updateNewMessage.message;
                LOGGER.info("UpdateNewMessage with message: " + message.toString());
                try {
                    telegramMessageService.saveIfNewTelegramMessage(message);
                } catch (InterruptedException e) {
                    LOGGER.error("Error during saveIfNewTelegramMessage", e);
                }
                break;
            case TdApi.UpdateMessageContent.CONSTRUCTOR:
                TdApi.UpdateMessageContent updateMessageContent = (TdApi.UpdateMessageContent) object;
                //TODO
                LOGGER.info("TODO UpdateMessageContent with message: " + updateMessageContent.toString());
            case TdApi.UpdateDeleteMessages.CONSTRUCTOR:
                TdApi.UpdateDeleteMessages updateDeleteMessages = (TdApi.UpdateDeleteMessages) object;
                LOGGER.info("UpdateDeleteMessages with message: " + updateDeleteMessages.toString());
                telegramMessageService.updateDeletedMessages(updateDeleteMessages);
                break;
//            case TdApi.UpdateMessageViews.CONSTRUCTOR:
//                TdApi.UpdateMessageViews updateMessageViews = (TdApi.UpdateMessageViews) object;
//                LOGGER.info("UpdateMessageViews: " + updateMessageViews.toString());
//                telegramMessageService.updateMessageViews(updateMessageViews);
            case TdApi.UpdateMessageEdited.CONSTRUCTOR:
                TdApi.UpdateMessageEdited updateMessageEdited = (TdApi.UpdateMessageEdited) object;
                LOGGER.info("UpdateMessageEdited: " + updateMessageEdited.toString());
                //TODO: save reactions
            default:
                LOGGER.info("Unsupported update:\n"  + object);
        }
    }

    private void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        LOGGER.info("onAuthorizationStateUpdated: " + authorizationState.getConstructor());
        switch (authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.databaseDirectory = "authDb";
                parameters.useMessageDatabase = true;
                parameters.useSecretChats = true;
                parameters.apiId = apiId;
                parameters.apiHash = apiHash;
                parameters.systemLanguageCode = "en";
                parameters.deviceModel = "Desktop";
                parameters.systemVersion = "Unknown";
                parameters.applicationVersion = "1.0";
                parameters.enableStorageOptimizer = true;

                client.send(new TdApi.AddProxy(proxyIp, proxyPort, true,
                        new TdApi.ProxyTypeSocks5(proxyUserName, proxyPassword)), new AddProxyRequestHandler());

                client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) authorizationState).link;
                System.out.println("Please confirm this login link on another device: " + link);
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                StringBuilder code = new StringBuilder();
                try {
                    while (code.toString().isEmpty()) {
                        File file = new File("/config/code.txt");
                        if (file.exists()) {
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String line;
                            while ((line = br.readLine()) != null) {
                                code.append(line);
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Could not read code.txt");
                }
                LOGGER.info("Code: " + code.toString());
                client.send(new TdApi.CheckAuthenticationCode(code.toString()), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
                String firstName = Utils.promptString("Please enter your first name: ");
                String lastName = Utils.promptString("Please enter your last name: ");
                client.send(new TdApi.RegisterUser(firstName, lastName), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
                String password = Utils.promptString("Please enter password: ");
                client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                authorizationLock.lock();
                try {
                    gotAuthorization.signal();
                } finally {
                    authorizationLock.unlock();
                }
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                LOGGER.info("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                LOGGER.info("Closing");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                LOGGER.info("Closed");
                break;
            default:
                System.err.println("Unsupported authorization state:\n" + authorizationState);
        }
    }
}
