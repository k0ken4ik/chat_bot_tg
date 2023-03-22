package tg.service;


import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tg.config.Config;
import tg.model.Ads;
import tg.model.AdsRepository;
import tg.model.User;
import tg.model.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdsRepository adsRepository;
    @Autowired
    private OpenAiService openAiService;
    final Config config;
    static final String ERROR_TEXT = "Error occured: ";
    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";
    static final String HELP_TEXT = "This bot is my first bot created on telegram platform.\n" +
            "You can execute commands from the main menu on left or by typing command \n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";

    public Bot(Config config) {
        this.config = config;

        List<BotCommand> botCommand = new ArrayList<>();
        botCommand.add(new BotCommand("/start", "Push me!"));
        botCommand.add(new BotCommand("/stop", "Bye!"));
        botCommand.add(new BotCommand("/mydata", "get your data stored"));
        botCommand.add(new BotCommand("/deletedata", "delete my data"));
        botCommand.add(new BotCommand("/help", "info how to use this bot"));
        botCommand.add(new BotCommand("/settings", "set your settings"));

        try{
            this.execute(new SetMyCommands(botCommand, new BotCommandScopeDefault(),null));
        } catch(TelegramApiException e){
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    private void startCommand(long chatId, String firstName){
        String answer = EmojiParser.parseToUnicode("I know you want to be an astronaut " + firstName + "!!!"
        + " :blush:");
        log.info("Replied to user " + firstName);
        sendMessage(chatId, answer);
    }

    private void stopCommand(long chatId, String firstName){
        String answer = "Bye, Bye, I believe in you " + firstName + "!!!";
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textMessage){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textMessage);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("weather");
        row.add("get random joke");
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("register");
        row.add("check my data");
        row.add("delete my data");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }

    @Override
    public void onUpdateReceived(@NonNull Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getChat().getFirstName();

            if(message.contains("/send") && config.getOwnerId() == chatId){
                var textToSend = EmojiParser.parseToUnicode(message.substring(message.indexOf(" ")));
                var users = userRepository.findAll();
                for(User user:users){
                    prepareAndSendMessage(user.getChatId(),textToSend);
                }
            }
            else {
                switch (message) {
                    case "/start":
                        startCommand(chatId, firstName);
                        registerUser(update.getMessage());
                        break;
                    case "/stop":
                        stopCommand(chatId, firstName);
                        break;
                    case "/register":
                        register(chatId);
                        break;
                    case "/help":
                        prepareAndSendMessage(chatId, HELP_TEXT);
                        break;
                    case "/chat" :
                        answerChatGpt(chatId);
                        break;
                    default:
                        prepareAndSendMessage(chatId, "Sorry, command not found");
                }
            }
        }

        else if(update.hasCallbackQuery()){
            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if(callBackData.equals(YES_BUTTON)){
                String text = "You pressed YES button";
                executeEditMessageText(text,chatId,messageId);
                
            } else if (callBackData.equals(NO_BUTTON)) {
                String text = "You pressed NO button";
                executeEditMessageText(text,chatId,messageId);
            }
        }
    }

    private void answerChatGpt(long chatId){

        CompletionRequest request = CompletionRequest.builder().model("text-davinci-003")
                .prompt("Give me the receipt of pasta")
                .maxTokens(2400)
                .n(1)
                .stop(null)
                .temperature(0.1)
                .build();

        com.theokanning.openai.completion.CompletionResult response = openAiService.createCompletion(request);
        String answer = response.getChoices().get(0).getText();
        System.out.println(answer);
        sendMessage(chatId,answer);

//        CompletionRequest completionRequest = CompletionRequest.builder()
//                .model("ada")
//                .prompt("Somebody once told me the world is gonna roll me")
//                .echo(true)
//                .user("testing")
//                .n(3)
//                .build();
//        openAiService.createCompletion(completionRequest).getChoices().forEach(System.out::println);
//
//
//        CreateImageRequest request = CreateImageRequest.builder()
//                .prompt("A cow breakdancing with a turtle")
//                .build();
//        String answer1 = openAiService.createImage(request).getData().get(0).getUrl();
//
//        System.out.println(answer1);
//        sendMessage(chatId, answer1);

//        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
//                .messages(Collections.singletonList(new ChatMessage("user", "Hello!"))) До билда
//                .user("testing").model("ada").build();
//
//        String answer1 = openAiService
//                .createChatCompletion(chatCompletionRequest)
//                .getChoices()
//                .get(0)
//                .getMessage()
//                .getContent();
//        System.out.println(answer1);
//        sendMessage(chatId, answer1);
    }
    private void executeEditMessageText(String text, long chatId, long messageId){
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);
        try {
            execute(message);
        } catch (TelegramApiException e){
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void register(long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want register?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData(YES_BUTTON);

        var noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        executeMessage(message);
    }

    private void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved" + user);
        }
    }

    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e){
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId,String textMessage){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textMessage);
        executeMessage(message);
    }

    @Scheduled(cron = "${cron.scheduler}")
    private void sendAds(){
        var ads = adsRepository.findAll();
        var users = userRepository.findAll();
        for(Ads ad: ads){
            for(User user:users){
                prepareAndSendMessage(user.getChatId(),ad.getAd());
            }
        }
    }

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
