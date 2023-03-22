package tg.config;

import com.theokanning.openai.service.OpenAiService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Data
@PropertySource("application.properties")
public class Config {

    @Value("${bot.name}")
    String name;
    @Value("${bot.token}")
    String token;
    @Value("${bot.owner}")
    Long ownerId;
    @Bean
    OpenAiService openAiService(){
        return new OpenAiService("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");
    }
}
