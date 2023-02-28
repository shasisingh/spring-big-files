package nl.shashi.playground.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * {@link FileOutputStream}
 */
@RestController
@RequestMapping(path = "/api/v1/administration/large-files")
public class FileUploadApi {

    public static final int BUFFER_SIZE = 8 *1024;
    @Value("${file-poller.output.directory}")
    private String outputDirectory;

    @PostMapping(value = "/upload")
    public ResponseEntity<String> uploadFiles(
            @RequestParam(value = "files") MultipartFile[] files,
            @RequestParam(value = "customerId") String customerId) {

        for (MultipartFile multipartFile : files) {
            var buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            try (var outStream = new FileOutputStream(Paths.get(outputDirectory).resolve(customerId).toString());
                    var stream = multipartFile.getInputStream()) {
                while ((bytesRead = stream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
            }

        }
        return ResponseEntity.ok("***** API Call Successful! ***** ");
    }

}


