package nl.shashi.playground.rest.large.files;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication
@EnableIntegration
public class RestLargeFilesApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestLargeFilesApplication.class, args);
	}


	@Bean
	public PublishSubscribeChannel errorChannel(){
		return new PublishSubscribeChannel(false);
	}
}
