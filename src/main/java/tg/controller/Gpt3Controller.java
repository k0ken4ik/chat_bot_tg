//import com.theokanning.openai.gpt3.models.CompletionResponse;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.concurrent.CompletableFuture;
//
//@RestController
//public class Gpt3Controller {
//
//    private final Gpt3Service gpt3Service;
//
//    public Gpt3Controller(Gpt3Service gpt3Service) {
//        this.gpt3Service = gpt3Service;
//    }
//
//    @GetMapping("/gpt3")
//    public CompletableFuture<CompletionResponse> getGpt3Response(@RequestParam("prompt") String prompt) {
//        return gpt3Service.sendRequest(prompt);
//    }
//}
