//import com.theokanning.openai.gpt3.Gpt3Service;
//import com.theokanning.openai.gpt3.models.CompletionRequest;
//import com.theokanning.openai.gpt3.models.CompletionResponse;
//import com.theokanning.openai.completion.chat.ChatCompletionResult;
//import com.theokanning.openai.completion.CompletionResult;
//import com.theokanning.openai.completion.CompletionRequest;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import com.theokanning.openai.service.OpenAiService;
//
//import java.util.concurrent.CompletableFuture;
//
//@Service
//public class Gpt3Service {
//
//    private final Gpt3Service gpt3Service;
//
//    public Gpt3Service(@Value("${openai.api.key}") String apiKey) {
//        this.gpt3Service = new Gpt3Service(apiKey);
//    }
//
//    public CompletableFuture<CompletionResponse> sendRequest(String prompt) {
//        CompletionRequest request = new CompletionRequest.Builder()
//                .withModel("text-davinci-002")
//                .withPrompt(prompt)
//                .withMaxTokens(50)
//                .withN(1)
//                .withStop(null)
//                .withTemperature(0.7)
//                .build();
//        return gpt3Service.completeAsync(request);
//    }
//}
