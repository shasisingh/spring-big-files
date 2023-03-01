package nl.shashi.playground.rest.large.files.config;

import nl.shashi.playground.rest.large.files.service.FileHandler;
import nl.shashi.playground.rest.large.files.webclient.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

import static nl.shashi.playground.rest.large.files.util.FileHelper.createDirectory;
import static nl.shashi.playground.rest.large.files.util.FileHelper.exists;

@Configuration
public class FileHandlerConfiguration {

    @Value("${file-poller.output.directory}")
    private String outputDirectory;

    @Bean
    public FileHandler fileHandler(ConcurrentMetadataStore concurrentMetadataStore, RestTemplate restTemplate, Client client) {
        return new FileHandler(concurrentMetadataStore, restTemplate, client);
    }

    @PostConstruct
    void createDir() throws IOException {
        var dir = new File(outputDirectory);
        if (!exists(dir)) {
            createDirectory(dir.getAbsolutePath());
        }
    }

}
