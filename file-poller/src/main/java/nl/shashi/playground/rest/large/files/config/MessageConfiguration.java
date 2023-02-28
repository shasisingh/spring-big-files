package nl.shashi.playground.rest.large.files.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.RecursiveDirectoryScanner;
import org.springframework.integration.file.filters.FileSystemPersistentAcceptOnceFileListFilter;
import org.springframework.integration.metadata.ConcurrentMetadataStore;

import java.io.File;

@Configuration
public class MessageConfiguration {

    private static final int MAX_DIR_DEPTH = 2;

    @Value("${file-poller.polling.input.directory}")
    private String pollingDirectory;

    @Bean
    public MessageSource<File> sourceDirectory(RecursiveDirectoryScanner directoryScanner) {
        var messageSource = new FileReadingMessageSource();
        messageSource.setDirectory(new File(pollingDirectory));
        messageSource.setAutoCreateDirectory(true);
        messageSource.setScanner(directoryScanner);
        messageSource.setScanEachPoll(true);
        return messageSource;
    }


    @Bean("directoryScanner")
    public RecursiveDirectoryScanner directoryScanner(ConcurrentMetadataStore concurrentMetadataStore) {
        var scanner = new RecursiveDirectoryScanner();
        scanner.setMaxDepth(MAX_DIR_DEPTH);
        scanner.setFilter(new FileSystemPersistentAcceptOnceFileListFilter(concurrentMetadataStore, "filePoller_"));
        return scanner;
    }
}
