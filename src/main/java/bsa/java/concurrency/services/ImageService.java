package bsa.java.concurrency.services;

import bsa.java.concurrency.services.fs.FileSystem;
import bsa.java.concurrency.services.hasher.DHasher;
import bsa.java.concurrency.image.Image;
import bsa.java.concurrency.image.ImageHashRepository;
import bsa.java.concurrency.image.dto.SearchResultDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Service
public class ImageService {
    private final ImageHashRepository repository;
    private final FileSystem fs;
    private final DHasher hasher;

    public ImageService(ImageHashRepository repository, FileSystem fs, DHasher hasher) {
        this.repository = repository;
        this.fs = fs;
        this.hasher = hasher;
    }

    public List<SearchResultDTO> searchMatches(byte[] img, double accuracy) throws IOException, ExecutionException, InterruptedException {
        long imgHash = hasher.calculateHash(img);

        List<SearchResultDTO> result = repository.getAllMatches(imgHash, accuracy);

        if (result.isEmpty()) {
            String path = fs.saveFile(img).get();

            repository.save(Image
                    .builder()
                    .hash(imgHash)
                    .path(path)
                    .build());
        }
        return result;
    }

    public void upload(MultipartFile[] files) {
        ExecutorService executor = Executors.newFixedThreadPool((files.length+1) * 2);

        Arrays
                .stream(files)
                .parallel()
                .forEach(processImage(executor));
    }

    private  Consumer<MultipartFile> processImage(ExecutorService executor) {
        return img -> {
            try {
                var bytes = img.getBytes();

                CompletableFuture<String> futurePath = executor.submit(() -> fs.saveFile(bytes).get());
                Future<Long> futureHash = executor.submit(() -> hasher.calculateHash(bytes));

                repository.save(Image
                        .builder()
                        .hash(futureHash.get())
                        .path(futurePath.get())
                        .build());

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    public void deleteImage(UUID id) throws IOException {

        var entity = repository.findById(id);

        if (entity.isPresent()) {
            repository.deleteById(id);
            fs.deleteOne(entity.get().getPath());
        }
    }

    public void fullWipe() throws IOException {
        repository.deleteAll();
        fs.deleteAll();
    }
}
