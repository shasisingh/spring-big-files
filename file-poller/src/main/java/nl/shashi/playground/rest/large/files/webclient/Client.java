package nl.shashi.playground.rest.large.files.webclient;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;

@Service
@AllArgsConstructor
@Slf4j
public class Client {

    private final WebClient webClient;

    public void post(File payload){
        var multipartBodyBuilder=buildRequest(payload);
        Mono<String> result = invokeRequest(multipartBodyBuilder);
        log.info("WebClient ==>{}", result.share().block());
    }

    private static MultipartBodyBuilder buildRequest(File payload) {
        var builder = new MultipartBodyBuilder();
        builder.part("files", new FileSystemResource(payload));
        builder.part("customerId", payload.getName());
        return builder;
    }

    private Mono<String> invokeRequest(MultipartBodyBuilder multipartBodyBuilder){
       return
                webClient
                .post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchangeToMono(response -> {
                    if (response.statusCode()== HttpStatus.OK) {
                        return response.bodyToMono(String.class);
                    } else {
                        throw new RuntimeException("Error uploading file");
                    }
                });
    }

}
