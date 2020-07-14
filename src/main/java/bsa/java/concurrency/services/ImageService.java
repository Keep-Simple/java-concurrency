package bsa.java.concurrency.services;

import bsa.java.concurrency.dto.IncomingImageDto;
import bsa.java.concurrency.dto.SearchResultDTO;
import bsa.java.concurrency.entity.Image;
import bsa.java.concurrency.repository.ImageHashRepository;
import bsa.java.concurrency.services.fs.FileSystem;
import bsa.java.concurrency.services.hasher.DHasher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
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

    public List<SearchResultDTO> searchMatches(IncomingImageDto dto, double accuracy) throws Exception {
        long imgHash = hasher.calculateHash(dto.getImg());

        List<SearchResultDTO> result = repository.getAllMatches(imgHash, accuracy);

        if (result.isEmpty()) {
            fs.saveFile(dto)
                    .thenAccept(p -> repository.save(Image
                            .builder()
                            .hash(imgHash)
                            .path(p)
                            .build()));
        }
        return result;
    }

    public void upload(List<IncomingImageDto> files) {
        files.parallelStream()
                .forEach(img -> {
                    try {
                        var futurePath = fs.saveFile(img);
                        var hash = hasher.calculateHash(img.getImg());

                        repository.save(Image
                                .builder()
                                .hash(hash)
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

//    public CompletableFuture<Long> getHash(byte[] img) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                return hasher.calculateHash(img);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }, executor);
//    }
}
