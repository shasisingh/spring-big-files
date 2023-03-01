package nl.shashi.playground.rest.large.files.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.shashi.playground.rest.large.files.webclient.Client;
import org.springframework.beans.factory.annotation.Value;
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

import static java.nio.file.Files.deleteIfExists;

@Slf4j
@RequiredArgsConstructor
public class FileHandler implements MessageHandler {

    @Value("${external.service.uri}")
    private String remoteUrl;

    @Value("${api.call.restTemplate:true}")
    private boolean isRestTemplateCall;


    private final ConcurrentMetadataStore concurrentMetadataStore;
    private final RestTemplate restTemplate;
    private final Client webClient;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {

        var payload = (File) message.getPayload();
        log.info("File received. fileName:{}", payload.getName());
        try {
            if (isRestTemplateCall) {
                postUsingRestTemplate(payload);
            } else {
                webClient.post(payload);
            }
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

    private void postUsingRestTemplate(File payload) throws IOException {
        var request = buildRequest(payload);
        String restResponse = restTemplate.postForObject(remoteUrl, request, String.class);
        log.info("RestTemplate ==>{}", restResponse);
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
