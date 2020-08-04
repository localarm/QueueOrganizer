package com.pavel.queueorganizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Bot extends TelegramLongPollingBot {
    private final static Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    private final QueueOrganizerApi queueOrganizerApi;
    private final String botToken;
    private final String botUsername;
    private final static String locationReplyButton = "Местоположение";
    // коды методов для передачи через callback
    private final static String queueInfoInline = "1";
    private final static String queueByAdminInline = "2";
    private final static String activateQueueInline = "3";
    private final static String deactivateQueueInline = "4";
    private final static String executorsListInline = "5";
    private final static String closeQueuePositiveInline = "6";
    private final static String closeQueueNegativeInline = "7";
    private final static String closeQueueInline = "8";
    private final static String getInQueueInline = "9";
    private final static String leaveQueueInline = "10";
    private final static String leaveQueuePositiveInline = "11";
    private final static String leaveQueueNegativeInline = "12";
    private final static String switchNotificationOnInline = "13";
    private final static String switchNotificationOffInline = "14";
    private final static String nextClientInline = "15";
    private final static String setQueueMenuForExecutorInline = "16";
    private final static String enterExecModeInline = "17";
    private final static String quitExecModeInline = "18";
    private final static String getClientsListInline = "19";
    // команды для взаимодействия с ботом
    private final static String queuesAroundCommand = "/find"; // команда бота для поиска очередей поблизости
    private final static String queuesAroundText = "Передайте свое местоположение для поиска очередей поблизости";
    private final static String createQueueCommand = "/create"; // команда бота для создания очереди
    private final static String createQueueText = "Передайте свое местоположение для создания очереди ";
    private final static String userQueuesCommand = "/myq"; // команда бота для вывода очередей пользователя
    private final static String executorQueuesCommand = "/exq";  // команда бота для вывода очередей исполнителя
    private final static String executorQueuesText = "Передайте свое местоположение для поиска очередей, в которых вы " +
            "являетесь исполнителем";
    private final static String start = "/start";
    // команда бота для добавления нового испонителя /add 'id очереди' 'id исполнителя'
    private final static String addCommand = "/add";
    // команда бота для увольнения испонителя /add 'id очереди' 'id исполнителя'
    private final static String fireCommand = "/fire";
    // команда бота для изменения имени исполнителя /rename 'id очереди' 'id исполнителя' 'новое имя исполнителя'
    private final static String changeNameCommand = "/rename";
    private final static String startText = "Добро пожаловать\nКоманды для взаимодействия с ботом\n" +
            queuesAroundCommand + " - поиск очередей поблизости\n" + createQueueCommand +" 'название очереди' - " +
            "создать очередь\n" + userQueuesCommand + " - очереди под вашим управлением\n" + addCommand + " " +
            "'id очереди' 'id исполнителя' 'имя исполнителя'  - для добавления исполнителя\n" + fireCommand + " 'id очереди' " +
            "'id исполнителя' - для увольнения исполнителя\n" + changeNameCommand + " 'id очереди' 'id исполнителя' " +
            "'новое имя исполнителя' - для изменения имени исполнителя\n" + executorQueuesCommand + " для вывода " +
            "очередей, где вы являетесь исполнителем";
    private final static HashMap<String, QuadConsumer<SendMessage, Long, Long, Long>> callbackHashMap =
            new HashMap<>();
    private final static HashMap<String, QuadConsumer<SendMessage, String[], Message, Location>> messageHashMap =
            new HashMap<>();

    {
        callbackHashMap.put(activateQueueInline, (sendMessage, queueId, userId, time) ->
                switchQueueActiveStatus(sendMessage, queueId, userId, true));
        callbackHashMap.put(deactivateQueueInline, (sendMessage, queueId, userId, time) ->
                switchQueueActiveStatus(sendMessage, queueId, userId, false));
        callbackHashMap.put(queueByAdminInline, (sendMessage, queueId, userId, time) ->
                setQueueMenuForAdmin(sendMessage, queueId));
        callbackHashMap.put(executorsListInline, (sendMessage, queueId, userId, time) ->
                getExecutorsList(sendMessage, queueId, userId));
        callbackHashMap.put(closeQueueInline, (sendMessage, queueId, userId, time) ->
                setCloseQueueConfirmationInline(sendMessage, queueId));
        callbackHashMap.put(closeQueuePositiveInline, (sendMessage, queueId, userId, time) -> closeQueue(sendMessage,
                queueId, userId));
        callbackHashMap.put(closeQueueNegativeInline, (sendMessage, queueId, userId, time) ->
                setQueueMenuForAdmin(sendMessage, queueId));
        callbackHashMap.put(getInQueueInline, this::getInQueue);
        callbackHashMap.put(leaveQueueInline, (sendMessage, queueId, userId, time) -> setLeaveQueueConfirmation(
                sendMessage, queueId));
        callbackHashMap.put(leaveQueuePositiveInline, (sendMessage, queueId, userId, time) ->
                leaveQueue(sendMessage, queueId, userId));
        callbackHashMap.put(leaveQueueNegativeInline, (sendMessage, queueId, userId, time) -> {
                sendMessage.setText("Вы остались в очереди");
                setClientInQueueMenu(sendMessage, userId, queueId);
                });
        callbackHashMap.put(switchNotificationOnInline, (sendMessage, queueId, userId, time) ->
                switchNotification(sendMessage, queueId, userId, true));
        callbackHashMap.put(switchNotificationOffInline, (sendMessage, queueId, userId, time) ->
                switchNotification(sendMessage, queueId, userId, false));
        callbackHashMap.put(nextClientInline, (sendMessage, queueId, userId, time) -> nextClient(sendMessage,
                queueId, userId));
        callbackHashMap.put(setQueueMenuForExecutorInline, (sendMessage, queueId, userId, time) ->
                setQueueMenuForExecutor(sendMessage, queueId));
        callbackHashMap.put(enterExecModeInline, (sendMessage, queueId, userId, time) -> enterExecutorMode(sendMessage,
                queueId, userId));
        callbackHashMap.put(quitExecModeInline, (sendMessage, queueId, userId, time) -> quitExecutorMode(sendMessage,
                queueId, userId));
        callbackHashMap.put(queueInfoInline, (sendMessage, queueId, userId, time) ->
                setQueueInfoForClient(sendMessage, queueId, time));
        callbackHashMap.put(getClientsListInline, (sendMessage, queueId, userId, time) -> getClientsList(sendMessage,
                queueId, userId));
    }

    {
        messageHashMap.put(userQueuesCommand, (sendMessage, textArray, message, location) ->
                setAdminQueuesInline(sendMessage, message.getFrom().getId()));
        messageHashMap.put(executorQueuesCommand,  (sendMessage, textArray, message, location) ->
                requestLocation(sendMessage, executorQueuesText));
        messageHashMap.put(createQueueCommand, (sendMessage, textArray, message, location) ->
                requestCreateLocation(sendMessage, textArray));
        messageHashMap.put(queuesAroundCommand, (sendMessage, textArray, message, location) ->
                requestLocation(sendMessage, queuesAroundText));
        messageHashMap.put(start, (sendMessage, textArray, message, location) ->
                setStartClientMenu(sendMessage, message));
        messageHashMap.put(addCommand, (sendMessage, textArray, message, location) ->
                addExecutor(sendMessage, message, textArray));
        messageHashMap.put(fireCommand, (sendMessage, textArray, message, location) ->
                fireExecutor(sendMessage, message, textArray));
        messageHashMap.put(changeNameCommand, (sendMessage, textArray, message, location) ->
                changeExecName(sendMessage, message, textArray));
        messageHashMap.put(queuesAroundText, (sendMessage, textArray, message, location) ->
                setQueuesAroundInline(sendMessage, location, textArray));
        messageHashMap.put(executorQueuesText, (sendMessage, textArray, message, location) ->
                setQueuesByExecutor(sendMessage, message, location));
        messageHashMap.put(createQueueText, this::createQueue);
    }

    public Bot(QueueOrganizerApi queueOrganizerApi, String botToken, String botUsername) {
        this.queueOrganizerApi = queueOrganizerApi;
        this.botToken = botToken;
        this.botUsername = botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        //Если пришел ответ на inline кнопку
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            // [0] - inline-команда,[1] - id очереди, [2] - время отправки местоположения для очередей поблизости
            String[] callbackData = callbackQuery.getData().split(" ");
            Long userId = Long.valueOf(callbackQuery.getFrom().getId());
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(userId);
            QuadConsumer<SendMessage, Long, Long, Long> inlineConsumer = callbackHashMap.get(callbackData[0]);
            if (inlineConsumer != null) {
                inlineConsumer.accept(sendMessage, Long.valueOf(callbackData[1]), userId, callbackData.length == 3 ?
                        Long.parseLong(callbackData[2]) : 0);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    LOGGER.error(e.getMessage());
                }
            } else {
                LOGGER.error("Should not happened with {} command", callbackData[0]);
            }
            //Если пришел текстовый ответ
        } else {
                if (update.hasMessage()) {
                    Message message = update.getMessage();
                    if (message.hasLocation()) {
                        //Чтобы убрать кнопку с передачей местоположения, иначе не передать инлайн клавиатуру
                        try {
                            execute(new SendMessage(message.getChatId(), "Ваше текущее местоположение принято")
                                    .setReplyMarkup(new ReplyKeyboardRemove()));
                        } catch (TelegramApiException e) {
                            LOGGER.error(e.getMessage());
                        }
                        String replyMessage = message.getReplyToMessage().getText();
                        if (replyMessage != null) {
                            String[] text = null;
                            // проверить и выделить название очереди из сообщения, если пришел ответ на запрос местоположения
                            // при создании очереди
                            if (replyMessage.startsWith(createQueueText)) {
                                text = new String[]{replyMessage.substring(createQueueText.length())};
                                replyMessage = createQueueText;
                            }
                            // для передачи времени вызова запроса местоположения при поиске очередей вокруг
                            if (replyMessage.equals(queuesAroundText)) {
                                text = new String[]{String.valueOf(System.currentTimeMillis())};
                            }
                            QuadConsumer<SendMessage, String[], Message, Location> messageConsumer =
                                    messageHashMap.get(replyMessage);
                            if (messageConsumer != null) {
                                SendMessage sendMessage = new SendMessage();
                                sendMessage.setChatId(message.getChatId());
                                messageConsumer.accept(sendMessage, text, message, message.getLocation());
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    LOGGER.error(e.getMessage());
                                }
                            } else {
                                LOGGER.error("Should not happened when asked location {} ", replyMessage);
                            }
                        }
                    } else if (message.hasText()) {
                        String[] textArray = message.getText().trim().split("\\s+");
                        QuadConsumer<SendMessage, String[], Message, Location> messageConsumer = messageHashMap
                                .get(textArray[0]);
                        if (messageConsumer != null) {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(message.getChatId());
                            messageConsumer.accept(sendMessage, textArray, message, null);
                            try {
                                execute(sendMessage);
                            } catch (TelegramApiException e) {
                                LOGGER.error(e.getMessage());
                            }
                        }
                    }
                }
            }

    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    /**Добавляет кнопку с запросом местоположения в выводимое сообщение
     * @param sendMessage сообщение, отправляемое клиенту
     */
    private void requestLocation(SendMessage sendMessage, String text) {
        ReplyKeyboardMarkup clientKeyboard = new ReplyKeyboardMarkup();
        clientKeyboard.setSelective(true);
        clientKeyboard.setResizeKeyboard(true);
        clientKeyboard.setOneTimeKeyboard(true);
        sendMessage.setReplyMarkup(clientKeyboard);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(new KeyboardButton(locationReplyButton).setRequestLocation(true));
        keyboardRows.add(firstRow);
        clientKeyboard.setKeyboard(keyboardRows);
        sendMessage.setText(text);
    }

    /**Добавляет кнопку с запросом местоположения для создания очереди в выводимое сообщение
     * @param sendMessage сообщение, отправляемое клиенту
     */
    private void requestCreateLocation(SendMessage sendMessage, String[] messageText) {
        if ( 1 < messageText.length) {
            String text = Arrays.stream(messageText).skip(1).collect(Collectors.joining(" "));
            requestLocation(sendMessage, text);
        } else {
            sendMessage.setText("Неверный формат команды. " + createQueueCommand + " 'название очереди'");
        }
    }

    /**Выводит меню клиента на экран
     * @param sendMessage сообщение, отправляемое клиенту
     * @param message сообщение от пользователя с информацией о нем
     */
    private void setStartClientMenu(SendMessage sendMessage, Message message){
        try {
            queueOrganizerApi.addClient(message.getFrom().getId(), message.getFrom().getFirstName(),
                    message.getFrom().getLastName());
            sendMessage.setText(startText);
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошла ошибка в работе приложения, введите команду /start еще раз");
        }
    }

    /**Добавляет в выводимое сообщение список очередей под управлением пользователя в виде inline кнопок
     * @param sendMessage сообщение, отправляемое клиенту
     * @param adminId id пользователя, запросившего список своих очередей
     */
    private void setAdminQueuesInline(SendMessage sendMessage, long adminId){
        try {
            List<Queue> queues = queueOrganizerApi.getQueuesByAdmin(adminId);
            if (!queues.isEmpty()) {
                InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                inlineKeyboard.setKeyboard(setupQueuesCallbackData(queues, queueByAdminInline, 0));
                sendMessage.setText("Список очередей под вашим управлением:").setReplyMarkup(inlineKeyboard);
            } else {
                sendMessage.setText("У вас нет активных очередей");
            }
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошла ошибка в работе приложения");
        }
    }

    /**Отправляет пользователю inline кнопки с возможностью изменить статус активности очереди и закрытия очереди
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     */
    private void setQueueMenuForAdmin(SendMessage sendMessage, long queueId){
        try {
            Queue queue = queueOrganizerApi.getQueueInfo(queueId);
            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> firstButtonsRow = new ArrayList<>();
            if (queue.isActive()){
                firstButtonsRow.add(new InlineKeyboardButton().setText("Активировать очередь")
                        .setCallbackData(setupCallbackData(activateQueueInline, queueId, 0)));
            } else {
                firstButtonsRow.add(new InlineKeyboardButton().setText("Приостановить очередь")
                        .setCallbackData(setupCallbackData(deactivateQueueInline, queueId, 0)));
            }
            List<InlineKeyboardButton> secondButtonsRow = new ArrayList<>();
            secondButtonsRow.add(new InlineKeyboardButton().setText("Список исполнителей")
                    .setCallbackData(setupCallbackData(executorsListInline, queueId, 0)));
            List<InlineKeyboardButton> thirdButtonsRow = new ArrayList<>();
            thirdButtonsRow.add(new InlineKeyboardButton().setText("Закрыть очередь")
                    .setCallbackData(setupCallbackData(closeQueueInline, queueId, 0)));
            buttons.add(firstButtonsRow);
            buttons.add(secondButtonsRow);
            buttons.add(thirdButtonsRow);
            inlineKeyboard.setKeyboard(buttons);
            sendMessage.setText("Очередь " + queue.getName() + " " + queueId + "\n"+ (queue.isActive()? "Активна" :
                    "Приостановлена")).setReplyMarkup(inlineKeyboard);
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошла ошибка в работе приложения");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        }
    }

    /**Выводит меню с подтверждением закрытия очереди
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     */
    private void setCloseQueueConfirmationInline(SendMessage sendMessage, long queueId){
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow = new ArrayList<>();
        inlineKeyboard.setKeyboard(buttons);
        buttonsRow.add(new InlineKeyboardButton().setText("Да")
                .setCallbackData(setupCallbackData(closeQueuePositiveInline, queueId, 0)));
        buttonsRow.add(new InlineKeyboardButton().setText("Нет")
                .setCallbackData(setupCallbackData(closeQueueNegativeInline, queueId, 0)));
        buttons.add(buttonsRow);
        sendMessage.setText("Вы точно хотите закрыть выбранную очередь?").setReplyMarkup(inlineKeyboard);
    }

    /**Закрывает выбранную очередь
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     * @param adminId айди администратора
     */
    private void closeQueue(SendMessage sendMessage, long queueId, long adminId){
        try {
            List<ClientInQueue> clients = queueOrganizerApi.closeQueue(adminId, queueId);
            sendMessage.setText("Очередь закрыта");
            if (!clients.isEmpty()) {
                new Thread(()-> notifyUsersAboutClosing(clients)).start();
            }
        } catch (AccessException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Вы не являетесь администратором данной очереди");
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данная очередь больше не существует");
        }
    }

    /**Оповещает клиентов, у которых включено оповещение, о закрытии очереди
     * @param clients список клиентов в очереди
     */
    private void notifyUsersAboutClosing(List<ClientInQueue> clients){
        for (ClientInQueue client : clients){
            if(client.getNotificationStatus()){
                try {
                    execute(new SendMessage(client.getClientId(), "Очередь была закрыта администратором"));
                } catch (TelegramApiException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }

    /**Пользователь увольняет выбранного исполнителя из очереди
     * @param sendMessage сообщение, отправляемое пользователю
     * @param message Message с информацией о пользователе
     * @param textArray массив строк с аргументами для команды /fire
     */
    private void fireExecutor(SendMessage sendMessage, Message message, String[] textArray){
        try {
            if (textArray.length!= 3){
                sendMessage.setText("Неверный формат команды. /fire 'id очереди' 'id исполнителя'");
            } else {
                long queueId;
                long executorId;
                try {
                    queueId = Long.parseLong(textArray[1]);
                    executorId = Long.parseLong(textArray[2]);
                } catch (NumberFormatException e) {
                    sendMessage.setText("Айди очереди и исполнителя должны состоять только из цифр");
                    return;
                }
                if (executorId == message.getFrom().getId()){
                    sendMessage.setText("Вы не можете уволить самого себя");
                } else {
                    queueOrganizerApi.fireExecutor(executorId, message.getFrom().getId(), queueId);
                    sendMessage.setText("Вы уволили выбранного исполнителя");
                }
            }
        } catch (AccessException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Вы не являетесь администратором данной очереди");
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        } catch (NonexistentExecutorIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Выбранного исполнителя не существует");
        }
    }

    /**Добавляет исполнителя в очередь. Максимум 100 исполнителей
     * @param sendMessage сообщение, отправляемое клиенту
     * @param message Message пользователя с информацией о нем
     * @param textArray массив строк, содержащий аргументы команды /add
     */
    private void addExecutor(SendMessage sendMessage, Message message, String[] textArray){
        try {
            if (textArray.length < 4 || textArray.length >5){
                sendMessage.setText("Неверный формат команды. /add 'id очереди' 'id исполнителя' 'имя исполнителя'. " +
                        "Не более одного пробела в имени");
            } else {
                long queueId;
                long executorId;
                try {
                    queueId = Long.parseLong(textArray[1]);
                    executorId = Long.parseLong(textArray[2]);
                    String executorName = Arrays.stream(textArray).skip(3).collect(Collectors.joining(" "));
                    queueOrganizerApi.addExecutor(executorId, message.getFrom().getId(), queueId, executorName);
                    sendMessage.setText("Исполнитель успешно добавлен в очередь");
                } catch (NumberFormatException e) {
                    sendMessage.setText("Айди очереди и исполнителя должны состоять только из цифр");
                }
            }
        } catch (AccessException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Вы не являетесь администратором данной очереди");
        } catch (OverlimitException e) {
            sendMessage.setText("В данной очереди уже максимальное количество исполнителей");
        } catch (NameCollisionException e) {
            sendMessage.setText("Исполнитель с таким именем уже существует");
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        } catch (NameCharsLimitException e) {
            sendMessage.setText("Слишком длинное имя для исполнителя");
        }
    }

    /**Выводит сообщение со списком исполнителей, в который входит имя исполнителя и статус обслуживания клиента
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     * @param adminId айди администратора очереди
     */
    private void getExecutorsList(SendMessage sendMessage, long queueId, long adminId){
        try {
            List<Executor> executorsList = queueOrganizerApi.getExecList(adminId, queueId);
            if (executorsList.isEmpty()){
                sendMessage.setText(" В очереди нет исполнителей");
            } else {
                StringBuilder builder = new StringBuilder();
                for (Executor executor : executorsList) {
                    builder.append(executor.getName()).append(" ").append(executor.getClientId());
                    if (executor.isServeClient()) {
                        builder.append(" обслуживает клиента");
                    }
                    builder.append("\n");
                }
                sendMessage.setText(builder.toString());
            }
            //необходимо отослать сообщение со списком исполнителей, чтобы была возможность добавить меню очереди
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            setQueueMenuForAdmin(sendMessage, queueId);
        } catch (AccessException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Вы не являетесь администратором данной очереди");
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
            setQueueMenuForAdmin(sendMessage, queueId);
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        }
    }

    /**Выводит сообщение со списком очередей поблизости
     * @param sendMessage сообщение, отправляемое клиенту
     * @param location долгота и широта в радианах
     * @param textArray массив с единственным значением - время передачи локации для поиска очередей поблизости {@link
     * #onUpdateReceived}, в милисекундах
     */
    private void setQueuesAroundInline(SendMessage sendMessage, Location location, String[] textArray) {
        try {
            if (location == null){
                sendMessage.setText("Вы не передали свое местоположение, попробуйте еще раз");// Не должно такого быть
            } else {
                List<Queue> queues = queueOrganizerApi.getQueuesAround(location.getLongitude(),
                        location.getLatitude());
                if (!queues.isEmpty()) {
                    InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                    inlineKeyboard.setKeyboard(setupQueuesCallbackData(queues, queueInfoInline,
                            Long.parseLong(textArray[0])));
                    sendMessage.setText("Список очередей поблизости:").setReplyMarkup(inlineKeyboard);
                } else {
                    sendMessage.setText("Рядом с вами нет доступных очередей");
                }
            }
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**Отправляет сообщение пользователю о выбранной очереди. Если очередь доступна для вступления, добавляет
     * соответствующую inline кнопку
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId id очереди
     * @param locationTime время передачи местоположения при поиске очередей поблизости {@link #onUpdateReceived(Update)}
     * в милисекундах
     */
    private void setQueueInfoForClient(SendMessage sendMessage, long queueId, long locationTime) {
        try {
            Queue queue = queueOrganizerApi.getQueueInfo(queueId);
            sendMessage.setText("Очередь " + queue.getName() + (queue.isActive() ? "\nМожно вступить" :
                    "\nПрием клиентов запрещен администратором на неопределенный срок"));
            if (queue.isActive()) {
                InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                List<InlineKeyboardButton> buttonsRow = new ArrayList<>();
                buttonsRow.add(new InlineKeyboardButton().setText("Вступить")
                        .setCallbackData(setupCallbackData(getInQueueInline, queue.getId(), locationTime)));
                buttons.add(buttonsRow);
                inlineKeyboard.setKeyboard(buttons);
                sendMessage.setReplyMarkup(inlineKeyboard);
            }
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошла ошибка в работе приложения");
        }
    }

    /**Пользователь вступает в выбранную очередь. Добавляет кнопку выхода из очереди с подтверждением. В случае неудачи
     * выводит сообщение с причиной отказа
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     * @param clientId айди клиента
     * @param locationTime время передачи местоположения при поиске очередей поблизости {@link #onUpdateReceived(Update)}
     */
    private void getInQueue(SendMessage sendMessage, long queueId, long clientId, long locationTime) {
        try {
            //проверка на время вызова поиска очередей поблизости
            if (System.currentTimeMillis() - locationTime < 600000) {
                Notify<Client> notify = queueOrganizerApi.getInQueue(clientId, queueId);
                //если есть ожидающий исполнитель
                if (notify != null) {
                    sendMessage.setText("Вас готов принять " + notify.getExecutor().getName());
                    //заполнение  и отправка сообщения для подходящего исполнителя
                    try {
                        String textForExecutor = "Ваш клиент " + notify.getCurrentClient().getFirstName();
                        // lastName не всегда есть у клиента
                        if(notify.getCurrentClient().getLastName() != null) {
                            textForExecutor = textForExecutor + " " + notify.getCurrentClient().getLastName();
                        }
                        SendMessage messageForExecutor = new SendMessage(notify.getExecutor().getClientId(),
                                textForExecutor);
                        setExecutorMenu(messageForExecutor, queueId);
                        execute(messageForExecutor);
                    } catch (TelegramApiException e) {
                    LOGGER.error(e.getMessage());
                    }
                } else {
                    sendMessage.setText("Вы вошли в очередь");
                }
                setClientInQueueMenu(sendMessage,clientId, queueId);
            } else {
                sendMessage.setText("Прошло больше 10 минут с момента запроса списка очередей");
            }
        } catch (OverlimitException e) {
            sendMessage.setText("Очередь переполнена");
        } catch (WrongWorkingTimeException e) {
            sendMessage.setText("Вы пытаетесь вступить в очередь в неправильный период работы");
        } catch (InactiveQueueException e) {
            sendMessage.setText("Вы пытаетесь вступить в неактивную очередь");
        } catch (WrongTimeException e) {
            sendMessage.setText("Вы пытаетесь вступить в очередь в нерабочие часы");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        } catch (RepeatedGetInTheQueueException e) {
            sendMessage.setText("Вы уже находитесь в очереди в другой очереди. Покиньте предыдущую очередь");
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        }
    }

    /**Выводит меню клиента, находящегося в очереди, c кнопками включения/выключения оповещений и возможностью выйти из
     * очереди
     * @param sendMessage сообщение, отправляемое клиенту
     * @param clientId айди пользователя
     * @param queueId айди очереди
     */
    private void setClientInQueueMenu(SendMessage sendMessage, long clientId, long queueId){
        try {
            ClientInQueue clientInQueue =  queueOrganizerApi.getClientInQueueInfo(clientId, queueId);
            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> firstButtonRow = new ArrayList<>();
            buttons.add(firstButtonRow);
            if (clientInQueue.getNotificationStatus()){
                firstButtonRow.add(new InlineKeyboardButton().setText("Отключить оповещения")
                        .setCallbackData(setupCallbackData(switchNotificationOffInline, queueId, 0)));
            } else {
                firstButtonRow.add(new InlineKeyboardButton().setText("Включить оповещения")
                        .setCallbackData(setupCallbackData(switchNotificationOnInline, queueId, 0)));
            }
            List<InlineKeyboardButton> secondButtonRow = new ArrayList<>();
            buttons.add(secondButtonRow);
            secondButtonRow.add(new InlineKeyboardButton().setText("Выйти из очереди")
                    .setCallbackData(setupCallbackData(leaveQueueInline, queueId, 0)));
            inlineKeyboard.setKeyboard(buttons);
            sendMessage.setReplyMarkup(inlineKeyboard);
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        } catch (NonexistentClientException e) {
            sendMessage.setText("Вы не находитесь в данной очереди");
        }
    }

    /**Выводит меню подтверждения выхода из очереди
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     */
    private void setLeaveQueueConfirmation(SendMessage sendMessage, long queueId){
        sendMessage.setText("Вы уверены что хотите покинуть очередь?");
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        buttons.add(buttonRow);
        buttonRow.add(new InlineKeyboardButton().setText("Да")
                .setCallbackData(setupCallbackData(leaveQueuePositiveInline, queueId, 0)));
        buttonRow.add(new InlineKeyboardButton().setText("Нет")
                .setCallbackData(setupCallbackData(leaveQueueNegativeInline, queueId, 0)));
        inlineKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(inlineKeyboard);
    }

    /**Изменяет статус оповещений у выбранного клиента в очереди
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     * @param clientId айди клиента в очереди
     * @param status включить/выключить оповещения
     */
    private void switchNotification(SendMessage sendMessage, long queueId, long clientId, boolean status){
        try {
            queueOrganizerApi.switchNotification(queueId, clientId, status);
            sendMessage.setText("Вы " + (status?"включили": "отключили") + " оповещения в очереди");
            setClientInQueueMenu(sendMessage, clientId, queueId);
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        } catch (NonexistentClientException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Вы не являетесь клиентом очереди");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        }
    }

    /**Пользователь покидает выбранную очередь
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     * @param clientId айди клиента
     */
    private void leaveQueue(SendMessage sendMessage, long queueId, long clientId){
        try {
            queueOrganizerApi.leaveQueue(queueId, clientId);
            sendMessage.setText("Вы покинули очередь");
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        } catch (NonexistentClientException e) {
            sendMessage.setText( "Вы не являетесь клиентом этой очереди");
        }
    }

    /**Выводит inline кнопки с возможностью принятия следующего клиента и выходом из режима исполнителя
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     */
    private void setExecutorMenu(SendMessage sendMessage, long queueId){
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        firstRow.add(new InlineKeyboardButton().setText("Следующий клиент")
                .setCallbackData(setupCallbackData(nextClientInline, queueId, 0)));
        buttons.add(firstRow);
        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        secondRow.add(new InlineKeyboardButton().setText("Выход")
                .setCallbackData(setupCallbackData(quitExecModeInline, queueId, 0)));
        buttons.add(secondRow);
        inlineKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(inlineKeyboard);
    }

    /**Принимает следующего клиента в очереди, текущий клиент считается обслуженным
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     * @param executorId айди клиента
     */
    private void nextClient(SendMessage sendMessage, long queueId, long executorId){
        try {
            Notify<ClientInQueue> notify =  queueOrganizerApi.nextClient(queueId, executorId);
            if (notify != null) {
                // отправляет сообщение клиенту, который должен сейчас обслуживаться
                try {
                    SendMessage messageForClient = new SendMessage(notify.getCurrentClient().getClientId(),
                            "Вас готов принять " + notify.getExecutor().getName());
                    setClientInQueueMenu(sendMessage, notify.getCurrentClient().getClientId(), queueId);
                    execute(messageForClient);
                } catch (TelegramApiException e) {
                    LOGGER.error(e.getMessage());
                }
                sendMessage.setText("Ваш следующий клиент " + notify.getCurrentClientName());

                //Если в очереди есть еще клиент, то уведомляет их об изменении позиции в очереди
                if (notify.getSecondClient() != null) {
                    try {
                        long secondClientId = notify.getSecondClient().getClientId();
                        SendMessage notifyAboutPlace = new SendMessage(secondClientId,
                                "Вы следующий в очереди");
                        setClientInQueueMenu(sendMessage, secondClientId, queueId);
                        execute(notifyAboutPlace);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                sendMessage.setText("Сейчас нет клиентов в очереди, вы встали в режим ожидания клиента");
            }
            setExecutorMenu(sendMessage, queueId);
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        } catch (AccessException e) {
            sendMessage.setText("Вы больше не являетесь исполнителем данной очереди");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        } catch (WrongTimeException e) {
            sendMessage.setText("Вы пытаетесь работать в нерабочий период очереди");
        } catch (NonexistentExecutorIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Вы не являетесь исполнителем данной очереди");
        } catch (WrongWorkingTimeException e) {
            sendMessage.setText("Вы пытаетесь принять клиента в нерабочие часы очереди");
        } catch (InactiveExecutorException e) {
            sendMessage.setText("Войдите в режим исполнителя для обслуживания клиентов");
        } catch (WaitingException e) {
            sendMessage.setText("Вы уже находитесь в режиме ожидания клиента");
        }
    }

    /**Включает режим исполнителя у пользователя. Добавляет кнопку следующего клиента и выхода из этого режима
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     * @param executorId айди исполнителя
     */
    private void enterExecutorMode(SendMessage sendMessage, long queueId, long executorId){
        try {
            queueOrganizerApi.enterExecutorMode(queueId, executorId);
            setExecutorMenu(sendMessage, queueId);
            sendMessage.setText("Вы успешно вошли в режим исполнителя");
        } catch (AccessException e) {
            sendMessage.setText("Вы больше не являетесь исполнителем данной очереди");
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        } catch (WrongLocationException e) {
            sendMessage.setText("Вы находитесь слишком далеко от очереди");
        } catch (NonexistentExecutorIdException e) {
            sendMessage.setText("Вы не являетесь исполнителем данной очереди");
        }
    }

    /**Пользователь покидает режим исполнителя. Обслуживание текущего клиента считается завершенным
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     * @param executorId айди исполнителя
     */
    private void quitExecutorMode(SendMessage sendMessage, long queueId, long executorId) {
        try {
            queueOrganizerApi.quitExecutorMode(queueId, executorId);
            sendMessage.setText("Вы покинули режим исполнителя очереди");
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        } catch (AccessException e) {
            sendMessage.setText("Вы больше не являетесь исполнителем данной очереди");
        } catch (NonexistentExecutorIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Вы не являетесь исполнителем данной очереди");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        }
    }

    /**Выводит пользователю список очередей inline кнопками, где он является исполнителем. В радиусе 50 метров
     * @param sendMessage сообщение, отправляемое клиенту
     * @param message Message с информацией о пользователе
     * @param location долгота и широта в радианах
     */
    private void setQueuesByExecutor(SendMessage sendMessage, Message message, Location location){
        try {
            sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
            List<Queue> queues = queueOrganizerApi.getQueuesListByExecutor(message.getFrom().getId(),
                    location.getLongitude(), location.getLatitude());
            if (queues.isEmpty()){
                sendMessage.setText("Поблизости нет очередей, где вы являетесь исполнителем");
            } else {
                InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                inlineKeyboard.setKeyboard(setupQueuesCallbackData(queues, setQueueMenuForExecutorInline, 0));
                sendMessage.setText("Список очередей поблизости, где вы являетесь исполнителем:");
                sendMessage.setReplyMarkup(inlineKeyboard);
            }
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        }
    }

    /**Выводит меню исполнителя с кнопками входа и выхода из режима исполнителя, списком обслуженных клиентов для
     * определенной очереди
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     */
    private void setQueueMenuForExecutor(SendMessage sendMessage, long queueId){
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> firstButtonRow = new ArrayList<>();
        buttons.add(firstButtonRow);
        firstButtonRow.add(new InlineKeyboardButton().setText("Войти в режим исполнителя")
                .setCallbackData(setupCallbackData(enterExecModeInline, queueId, 0)));
        List<InlineKeyboardButton> secondButtonRow = new ArrayList<>();
        buttons.add(secondButtonRow);
        List<InlineKeyboardButton> thirdButtonRow = new ArrayList<>();
        secondButtonRow.add(new InlineKeyboardButton().setText("Выйти из режима исполнителя")
                .setCallbackData(setupCallbackData(quitExecModeInline, queueId, 0)));
        thirdButtonRow.add(new InlineKeyboardButton().setText("Список обслуженных клиентов")
                .setCallbackData(setupCallbackData(getClientsListInline, queueId, 0)));
        buttons.add(thirdButtonRow);
        inlineKeyboard.setKeyboard(buttons);
        sendMessage.setText("Меню исполнителя").setReplyMarkup(inlineKeyboard);
    }

    /**Создает очередь
     * @param sendMessage сообщение, отправляемое клиенту
     * @param textArray массив с единственным элементом - назание очереди
     * @param message Message с информацией о пользователе
     * @param location широта и долгота в радианах
     */
    private void createQueue(SendMessage sendMessage, String[] textArray, Message message, Location location) {
        try {
            sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
            queueOrganizerApi.createQueue(textArray[0], message.getFrom().getFirstName(), message.getFrom().getLastName(),
                    location.getLongitude(), location.getLatitude(), message.getFrom().getId());
            sendMessage.setText("Очередь " + textArray[0] + " создана");
        } catch (OverlimitException e) {
            sendMessage.setText("Вы превысили максимально доступное количество очередей");
        }  catch (NameCollisionException e) {
            sendMessage.setText("Очередь с таким именем уже существует");
        } catch (NameCharsLimitException e) {
            sendMessage.setText("Название очереди должно содержать меньше символов");
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("hпроизошла ошибка в работе приложения");
        }
    }

    /**Меняет статус активности у выбранной очереди
     * @param sendMessage сообщение, отправляемое клиенту
     * @param adminId айди администратора
     * @param queueId айди очереди
     * @param status статус активности очереди
     */
    private void switchQueueActiveStatus(SendMessage sendMessage, long queueId, long adminId, boolean status){
        try {
            queueOrganizerApi.switchQueueActiveStatus(adminId, queueId, status);
            sendMessage.setText(status ? "Вы успешно активировали очередь " : "Вы успешно приостановили очередь");
            setQueueMenuForAdmin(sendMessage, queueId);
        } catch (AccessException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Вы не являетесь администратором данной очереди");
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        }
    }

    /**Изменяет имя выбранного исполнителя на новое
     * @param sendMessage сообщение, отправляемое клиенту
     * @param message айди исполнителя
     * @param textArray айди очереди
     */
    private void changeExecName(SendMessage sendMessage, Message message, String[] textArray){
        try {
            if (textArray.length < 4 || textArray.length >5){
                sendMessage.setText("Неверный формат команды. /rename 'id очереди' 'id исполнителя' 'новое имя " +
                        "исполнителя'");
            } else {
                long queueId;
                long executorId;
                try {
                    queueId = Long.parseLong(textArray[1]);
                    executorId = Long.parseLong(textArray[2]);
                    //пропустить номер callback команды, айди очереди и исполнителя
                    String newName = Arrays.stream(textArray).skip(3).collect(Collectors.joining(" "));
                    queueOrganizerApi.changeExecName(executorId, queueId, message.getFrom().getId(), newName);
                    sendMessage.setText("Вы успешно сменили имя исполнителя на " + newName);
                } catch (NumberFormatException e) {
                    sendMessage.setText("Айди очереди и исполнителя должны состоять только из цифр");
                }
            }
        } catch (AccessException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Вы не являетесь администратором данной очереди");
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        } catch (NameCollisionException e) {
            sendMessage.setText("Исполнитель с таким именем уже находится в очереди");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        } catch (NonexistentExecutorIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Выбранного исполнителя не существует");
        } catch (NameCharsLimitException e) {
            sendMessage.setText("Имя исполнителя должно содержать меньше символов");
        }
    }

    /**Выводит список клиентов, обслуженных исполнителем.Ввыводятся последние 20 обслуженных клиентов
     * @param sendMessage сообщение, отправляемое клиенту
     * @param queueId айди очереди
     * @param execId айди исполнителя
     */
    private void getClientsList(SendMessage sendMessage, long queueId, long execId){
        try {
            List<ServedClient> clientsList = queueOrganizerApi.getClientsList(queueId, execId);
            if (clientsList.isEmpty()){
                sendMessage.setText("У вас пока еще нет обслуженных клиентов");
            } else {
                StringBuilder builder = new StringBuilder();
                for (ServedClient client : clientsList) {
                    LocalDateTime startDateTime = client.getStartTime().toLocalDateTime(); //timestamp.toInstant().atZone(zoneId).toLocalDate()
                    LocalDate startDate = startDateTime.toLocalDate();
                    LocalTime startTime = startDateTime.toLocalTime();
                    LocalTime endTime = client.getEndTime().toLocalDateTime().toLocalTime();
                    builder.append(client.getName()).append(" ")
                            .append(startDate.getDayOfMonth()).append(".").append(startDate.getMonthValue())
                            .append(".").append(startDate.getYear()).append(" Начало ")
                            .append(startTime.getHour()).append(":").append(startTime.getMinute()).append(":")
                            .append(startTime.getSecond()).append(" Конец ").append(endTime.getHour())
                            .append(":").append(endTime.getMinute()).append(":").append(endTime.getSecond())
                            .append("\n");
                }
                sendMessage.setText(builder.toString());
            }
            setExecutorMenu(sendMessage, queueId);
        } catch (QueueOrganizerException e) {
            LOGGER.error(e.getMessage());
            sendMessage.setText("Произошел сбой в работе приложения");
        } catch (NonexistentExecutorIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Выбранного исполнителя не существует");
        } catch (AccessException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Вы не являетесь исполнителем данной очереди");
        } catch (NonexistentQueueIdException e) {
            LOGGER.warn(e.getMessage());
            sendMessage.setText("Такой очереди не существует");
        } catch (ClosedQueueException e) {
            sendMessage.setText("Данной очереди больше не существует");
        }
    }

    private String setupCallbackData(String command, long queueId, long time){
        String callbackData = command + " " + queueId;
        if (time != 0){
            callbackData = callbackData + " " + time;
        }
        return callbackData;
    }

    private List<List<InlineKeyboardButton>> setupQueuesCallbackData(List<Queue> queues, String callback, long time) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (Queue queue : queues) {
            List<InlineKeyboardButton> buttonsRow = new ArrayList<>();
            buttonsRow.add(new InlineKeyboardButton().setText(queue.getName())
                    .setCallbackData(setupCallbackData(callback, queue.getId(), time)));
            buttons.add(buttonsRow);
        }
        return buttons;
    }
}
