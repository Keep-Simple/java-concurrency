package bsa.java.concurrency.controller;

import bsa.java.concurrency.dto.SearchResultDTO;
import bsa.java.concurrency.services.ImageService;
import bsa.java.concurrency.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    private ImageService service;

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public String handleException(Exception ex) {
        return ex.getMessage();
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public CompletableFuture<Void> batchUploadImages(@RequestParam("images") MultipartFile[] files) throws ExecutionException, InterruptedException {
        return service.upload(Arrays
                .stream(files)
                .map(Mapper::reqToDto)
                .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<SearchResultDTO> searchMatches(
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "threshold", defaultValue = "0.9") double threshold
    ) throws Exception {

        if (threshold <= 0 || threshold > 1) {
            throw new Exception("Provide correct threshold: (0, 1]");
        }

        return service.searchMatches(Mapper.reqToDto(file), threshold);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable("id") UUID imageId) throws IOException {
        service.deleteImage(imageId);
    }

    @DeleteMapping("/purge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void purgeImages() throws IOException {
        service.fullWipe();
    }
}
