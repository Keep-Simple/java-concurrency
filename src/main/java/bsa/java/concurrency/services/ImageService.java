package bsa.java.concurrency.services;

import bsa.java.concurrency.image.Image;
import bsa.java.concurrency.image.ImageHashRepository;
import bsa.java.concurrency.image.dto.SearchResultDTO;
import bsa.java.concurrency.services.fs.FileSystem;
import bsa.java.concurrency.services.hasher.DHasher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ImageService {
    private final ImageHashRepository repository;
    private final FileSystem fs;
    private final DHasher hasher;
    private final ExecutorService executor = Executors.newFixedThreadPool(6);

    public ImageService(ImageHashRepository repository, FileSystem fs, DHasher hasher) {
        this.repository = repository;
        this.fs = fs;
        this.hasher = hasher;
    }

    public List<SearchResultDTO> searchMatches(byte[] img, double accuracy) throws IOException, ExecutionException, InterruptedException {
        long imgHash = getHash(img).get();

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

    public void upload(List<byte[]> files) {
        files.parallelStream().forEach(img -> {
            try {
                var futurePath = fs.saveFile(img);
                var futureHash = getHash(img);

                repository.save(Image
                        .builder()
                        .hash(futureHash.get())
                        .path(futurePath.get())
                        .build());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
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

    public CompletableFuture<Long> getHash(byte[] img) {
        return CompletableFuture.supplyAsync(()-> {
            try {
                return hasher.calculateHash(img);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
}
