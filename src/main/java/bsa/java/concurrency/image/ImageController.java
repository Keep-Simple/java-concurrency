package bsa.java.concurrency.image;

import bsa.java.concurrency.image.dto.SearchResultDTO;
import bsa.java.concurrency.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

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
    public void batchUploadImages(@RequestParam("images") MultipartFile[] files) throws Exception {
        service.upload(files);
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

        return service.searchMatches(file.getBytes(), threshold);
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
