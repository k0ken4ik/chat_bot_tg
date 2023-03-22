package tg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableJpaRepositories
@SpringBootApplication
public class BotHelperApplication {

	public static void main(String[] args) {
		SpringApplication.run(BotHelperApplication.class, args);
	}

}
