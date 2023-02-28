package nl.shashi.playground.rest.large.files.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static java.nio.file.Files.deleteIfExists;

@Slf4j
@AllArgsConstructor
public class FileHandler implements MessageHandler {

    public static final String REMOTE_URL = "http://127.0.0.1:8081/api/v1/administration/large-files/upload";

    private final ConcurrentMetadataStore concurrentMetadataStore;
    private final RestTemplate restTemplate;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {

        var payload = (File) message.getPayload();
        log.info("File received. fileName:{}", payload.getName());
        try {
            post(payload);
            tableMetaDataCleanup(payload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void tableMetaDataCleanup(final File messagePayload) throws IOException {
        deleteFileFromProcessing(messagePayload);
        concurrentMetadataStore.remove("filePoller_" + messagePayload.getAbsolutePath());
        log.info("Database cleanup has been initiated, filename:{}", messagePayload.getPath());
    }

    private void post(File payload) throws IOException {
        Files.writeString(payload.toPath(), UUID.randomUUID().toString().concat(payload.getName()));

        var request = buildRequest(payload);
        String restResponse = restTemplate.postForObject(REMOTE_URL, request, String.class);
        log.info(restResponse);
    }

    private void deleteFileFromProcessing(final File messagePayload) throws IOException {
        deleteIfExists(messagePayload.toPath());
    }

    private HttpEntity<MultiValueMap<String, Object>> buildRequest(File payload) {
        var header = new HttpHeaders();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("files", new FileSystemResource(payload));
        map.add("customerId", payload.getName());
        return new HttpEntity<>(map, header);
    }

}
