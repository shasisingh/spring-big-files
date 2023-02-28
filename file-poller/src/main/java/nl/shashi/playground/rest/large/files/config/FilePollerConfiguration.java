package nl.shashi.playground.rest.large.files.config;

import nl.shashi.playground.rest.large.files.service.FileHandler;
import nl.shashi.playground.rest.large.files.util.FilePollerUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Configuration
public class FilePollerConfiguration {

    private static final String FILE_POLLER_ROLE = "leader";

    @Value("${filePoller.fixedTime.millisecond}")
    private int filePollerFixedDelayTimeInMillisecond;

    @Value("${filePoller.maxMessagePerPoll}")
    private int filePollerMaxMessagePerPoll;


    @Bean
    public IntegrationFlow filePoller(MessageSource<File> sourceDirectory, FileHandler fileHandler) {
        return IntegrationFlows.from(sourceDirectory,
                configurer ->
                        configurer
                                .role(FILE_POLLER_ROLE)
                                .autoStartup(false)
                                .poller(Pollers.fixedDelay(filePollerFixedDelayTimeInMillisecond, TimeUnit.MILLISECONDS)
                                        .maxMessagesPerPoll(filePollerMaxMessagePerPoll))
        ).filter(FilePollerUtility::onlyFiles)
                .filter(FilePollerUtility::fileExists)
                .handle(fileHandler)
                .get();
    }

    @Bean
    public RestTemplate restTemplate(SimpleClientHttpRequestFactory simpleClientHttpRequestFactory) {
        return new RestTemplate(simpleClientHttpRequestFactory);
    }

    /**
     * only with HTTP1.1 and that HTTP 2 no longer supports chunked transfer encoding; I think you have to look for some sort of streaming API.
     * @return SimpleClientHttpRequestFactory
     */
    @Bean
    public SimpleClientHttpRequestFactory requestFactory(){
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false);
        return requestFactory;
    }
}
