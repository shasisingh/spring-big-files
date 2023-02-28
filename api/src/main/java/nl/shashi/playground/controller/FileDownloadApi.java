package nl.shashi.playground.controller;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileInputStream;
import java.nio.file.Paths;

import static java.lang.String.format;

/**
 * Streaming large file through REST <br>
 * A good alternative that solves the memory problem is streaming the file.
 * We can take advantage of another type of resource - {@link InputStreamResource}
 */
@RestController
@RequestMapping(path = "/api/v1/administration/large-files")
public class FileDownloadApi {

    @Value("${file-poller.output.directory}")
    private String downloadDirectory;

    @SneakyThrows
    @GetMapping(value = "/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam(value = "filename", required = true) String filename) {

        var file = Paths.get(downloadDirectory).resolve(filename).toFile();
        if (!file.exists()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, format("file not found [file:%s].", filename));
        }

        Resource resource = new InputStreamResource(new FileInputStream(file));

        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, format("attachment; filename=%s",filename));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}


